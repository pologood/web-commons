package com.lianjia.iprd.view.view;

import com.lianjia.iprd.common.ErrorCode;
import com.lianjia.iprd.view.annotation.Cell;
import com.lianjia.iprd.view.annotation.Sheet;
import com.lianjia.iprd.view.common.ExcelException;
import com.lianjia.iprd.view.common.SheetBean;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 使用注解 @Sheet 和 @Cell,生成 ExcelView
 * Created by fengxiao on 16/5/30.
 */
public class AnnotatedExcelView extends AbstractExcelView {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DUMP_TIME_FORMAT = "_yyyy-MM-dd_HH-mm-ss";
    private static final String Excel_POSTFIX = ".xls";
    private static final Integer WAIT_TIMEOUT = 2;
    private static final Integer ROW_ACCESS_WINDOW_SIZE = 100;
    private static ThreadLocal<LinkedBlockingQueue<ExcelDumpBean>> threadLocal = new ThreadLocal<>();

    @Override
    protected void buildExcelDocument(Map<String, Object> model, HSSFWorkbook workbook, HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        Long startTime = System.currentTimeMillis();
        ExcelDumpBean dumpBean = getJobQueue().poll(WAIT_TIMEOUT, TimeUnit.SECONDS);
        if (dumpBean == null) {
            throw new ExcelException(ErrorCode.EXCEL_DUMP_ERROR);
        }
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            response.setContentType("application/x-download");
            response.addHeader("Content-Disposition", "attachment;filename=" + dumpBean.getExcelName());
            dumpBean.getWorkbook().write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        Long endTime = System.currentTimeMillis();
        logger.debug("generate excel for dump cost time is {}ms", endTime - startTime);
    }

    public AnnotatedExcelView(String excelName, SheetBean<?>... sheets) throws ExcelException {
        this(excelName, true, sheets);
    }

    public AnnotatedExcelView(String excelName, boolean postfixWithDate, SheetBean<?>... sheets) throws ExcelException {
        this(excelName, postfixWithDate, DUMP_TIME_FORMAT, sheets);
    }

    /**
     * 生成 excel 表单数据模型
     *
     * @param excelName       excel 名字
     * @param postfixWithDate 文件后缀加时间戳
     * @param dateFormat      时间戳格式
     * @param sheets          表单数据
     * @throws ExcelException
     */
    public AnnotatedExcelView(String excelName, boolean postfixWithDate, String dateFormat, SheetBean<?>... sheets)
            throws ExcelException {
        Long startTime = System.currentTimeMillis();
        if (ArrayUtils.isEmpty(sheets)) {
            throw new ExcelException(ErrorCode.EXCEL_SHEET_NOT_EXIST_ERROR);
        }

        createExcel(excelName, postfixWithDate, dateFormat, Arrays.asList(sheets));
        Long endTime = System.currentTimeMillis();
        logger.debug("generate HssWorkbook cost time is {}ms", endTime - startTime);
    }

    private void createExcel(String excelName, boolean postfix, String dateFormat, List<SheetBean<?>> beanList)
            throws ExcelException {
        Collections.sort(beanList, new Comparator<SheetBean<?>>() {
            @Override
            public int compare(SheetBean<?> o1, SheetBean<?> o2) {
                Sheet s1 = o1.getClazz().getAnnotation(Sheet.class);
                Sheet s2 = o2.getClazz().getAnnotation(Sheet.class);
                if (s1 == null || s2 == null) {
                    return NumberUtils.INTEGER_ZERO;
                }
                return s1.index() < s2.index() ? NumberUtils.INTEGER_MINUS_ONE : s1.index() == s2.index() ?
                        NumberUtils.INTEGER_ZERO : NumberUtils.INTEGER_ONE;
            }
        });

        SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
        workbook.setCompressTempFiles(true);

        ExcelDumpBean dumpBean = new ExcelDumpBean(workbook);
        for (SheetBean<?> sheetObj : beanList) {
            Class<?> clazz = sheetObj.getClazz();
            Sheet sheet;
            if (clazz == null || (sheet = clazz.getAnnotation(Sheet.class)) == null) {
                continue;
            }
            assembleSheet(clazz.getDeclaredFields(), dumpBean.getWorkbook().createSheet(sheet.name()), sheetObj.
                    getDataList());
        }

        dumpBean.setExcelName(StringUtils.join(excelName, postfix ? DateFormatUtils.format(System.currentTimeMillis(),
                dateFormat) : StringUtils.EMPTY, Excel_POSTFIX));
        putJob(dumpBean);
    }

    private void assembleSheet(Field[] fields, org.apache.poi.ss.usermodel.Sheet sheet, List<?> data)
            throws ExcelException {
        int columnCount = NumberUtils.INTEGER_ZERO;
        int rowCount = NumberUtils.INTEGER_ZERO;

        Row header = sheet.createRow(rowCount++);
        List<Field> sortedFields = Arrays.asList(fields);
        Collections.sort(sortedFields, new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                Cell c1 = o1.getClass().getAnnotation(Cell.class);
                Cell c2 = o2.getClass().getAnnotation(Cell.class);
                if (c1 == null || c2 == null) {
                    return NumberUtils.INTEGER_ZERO;
                }
                return c1.column() < c2.column() ? NumberUtils.INTEGER_MINUS_ONE : c1.column() == c2.column() ?
                        NumberUtils.INTEGER_ZERO : NumberUtils.INTEGER_ONE;
            }
        });

        for (Field field : sortedFields) {
            Cell cell = field.getAnnotation(Cell.class);
            if (cell == null) {
                continue;
            }
            header.createCell(columnCount++).setCellValue(cell.name());
        }

        for (Object obj : data) {
            columnCount = NumberUtils.INTEGER_ZERO;
            Row row = sheet.createRow(rowCount++);
            for (Field field : sortedFields) {
                field.setAccessible(true);
                try {
                    row.createCell(columnCount++).setCellValue(field.get(obj) == null ? field.getAnnotation(
                            Cell.class).defaultValue() : String.valueOf(field.get(obj)));
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                    throw new ExcelException(ErrorCode.FIELD_ILLEGAL_ACCESS_ERROR);
                }
            }
        }
    }

    private void putJob(ExcelDumpBean bean) throws ExcelException {
        try {
            getJobQueue().put(bean);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new ExcelException(ErrorCode.EXCEL_DUMP_ERROR);
        }
    }

    private BlockingQueue<ExcelDumpBean> getJobQueue() {
        if (threadLocal.get() == null) {
            threadLocal.set(new LinkedBlockingQueue<ExcelDumpBean>());
        }
        return threadLocal.get();
    }

    private class ExcelDumpBean {
        Workbook workbook;
        String excelName;

        public ExcelDumpBean(Workbook workbook) {
            this.workbook = workbook;
        }

        public Workbook getWorkbook() {
            return workbook;
        }

        public String getExcelName() {
            return excelName;
        }

        public void setExcelName(String excelName) {
            this.excelName = excelName;
        }
    }

}
