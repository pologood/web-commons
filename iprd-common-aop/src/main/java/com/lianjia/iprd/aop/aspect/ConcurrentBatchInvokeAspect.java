package com.lianjia.iprd.aop.aspect;

import com.lianjia.iprd.aop.annotation.BatchArray;
import com.lianjia.iprd.aop.annotation.ConcurrentBatchInvoke;
import com.lianjia.iprd.aop.common.AopException;
import com.lianjia.iprd.common.ErrorCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by fengxiao on 16/6/16.
 */
@Aspect
public class ConcurrentBatchInvokeAspect implements Ordered {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution( * com.lianjia.crm.service..*.*(..) ) && @annotation(concurrentBatchInvoke)")
    public Object around(ProceedingJoinPoint pjp, ConcurrentBatchInvoke concurrentBatchInvoke) throws Throwable {
        Object[] args = pjp.getArgs();
        logger.debug("Invoke {}.{}({})", pjp.getTarget().getClass().getSimpleName(), pjp.getSignature().getName(),
                args);

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Annotation[] annotations = method.getParameterAnnotations()[args.length - 1];

        if (!(args[args.length - 1] instanceof Object[])) {
            logger.error("param {} is not array", args[args.length - 1]);
            throw new AopException(ErrorCode.ANNOTATION_ILLEGAL_USE);
        }

        if (method.getReturnType().isAssignableFrom(Collection.class)) {
            logger.error("return type must be Collection");
            throw new AopException(ErrorCode.ANNOTATION_ILLEGAL_USE);
        }

        Object[] array = (Object[]) args[args.length - 1];

        boolean batchArrayExist = false;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(BatchArray.class)) {
                batchArrayExist = true;
            }
        }

        if (!batchArrayExist) {
            return pjp.proceed();
        }


        return concurrentBatch(pjp, args, array, concurrentBatchInvoke.pageSize());
    }

    /**
     * 将 objs 切分成相同大小的几份,大小为 pageSize,并发调用接口,合并结果
     */
    private Collection concurrentBatch(ProceedingJoinPoint pjp, Object[] args, Object[] objs, int pageSize)
            throws AopException {
        int totalPage = objs.length / pageSize + (objs.length % pageSize == NumberUtils.INTEGER_ZERO ? NumberUtils.
                INTEGER_ZERO : NumberUtils.INTEGER_ONE);

        Collection result = null;
        List<Future<Collection>> futureList = new LinkedList<>();
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = NumberUtils.INTEGER_ZERO; i < totalPage; i++) {
            Object[] tmpObjs = ArrayUtils.subarray(objs, i * pageSize, i == totalPage ? objs.length : (i + 1) *
                    pageSize);
            Object[] workerArgs = ArrayUtils.clone(args);//copy 一份,防止出现同步问题
            workerArgs[workerArgs.length - 1] = tmpObjs;
            futureList.add(executorService.submit(new InvokeWorker(pjp, workerArgs)));
        }
        try {
            Long startTime = System.currentTimeMillis();
            for (Future<Collection> future : futureList) {
                Collection collection = future.get();
                if (CollectionUtils.isEmpty(collection)) {
                    continue;
                }
                if (result == null) {
                    result = collection;
                } else {
                    result.addAll(collection);
                }
            }
            logger.debug("concurrent invoke method [{}] total invoke {} times, total cost {}ms, total size is {}",
                    ((MethodSignature) pjp.getSignature()).getMethod(), futureList.size(), System.currentTimeMillis()
                            - startTime, result == null ? NumberUtils.INTEGER_ZERO : result.size());
            return result == null ? Collections.EMPTY_LIST : result;
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new AopException(ErrorCode.INVOKE_REMOTE_API_ERROR);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
            throw new AopException(ErrorCode.INVOKE_REMOTE_API_ERROR);
        }
    }

    private class InvokeWorker implements Callable<Collection> {

        private ProceedingJoinPoint pjp;
        private Object[] args;

        InvokeWorker(ProceedingJoinPoint pjp, Object[] args) {
            this.pjp = pjp;
            this.args = args;
        }

        @Override
        public Collection call() throws Exception {
            try {
                return (Collection) pjp.proceed(args);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
            return null;
        }
    }

    public int getOrder() {
        return 1;
    }
}
