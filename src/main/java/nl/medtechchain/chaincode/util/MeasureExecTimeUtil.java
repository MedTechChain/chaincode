package nl.medtechchain.chaincode.util;

import java.util.function.Supplier;
import java.util.logging.Logger;

public class MeasureExecTimeUtil {

    private static final Logger logger = Logger.getLogger(MeasureExecTimeUtil.class.getName());

    public static <T> T monitorTime(Supplier<T> supplier) {
        var startTime = System.nanoTime();
        var result = supplier.get();
        var endTime = System.nanoTime();
        var executionTime = endTime - startTime;
        logger.info("Query execution time: " + executionTime);
        return result;
    }
}
