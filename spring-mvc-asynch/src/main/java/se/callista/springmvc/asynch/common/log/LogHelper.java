package se.callista.springmvc.asynch.common.log;

import com.sun.management.UnixOperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by magnus on 18/05/14.
 */
public class LogHelper {

    private static OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private static final AtomicLong lastRequestId = new AtomicLong(0);
    private static final AtomicLong concurrentRequests = new AtomicLong(0);
    private static long maxConcurrentRequests = 0;

    private final String name;
    private final Logger log;
    private final int requestsPerLog;
    private final long reqId;

    public LogHelper(Class clz, String name, int requestsPerLog) {
        this.log = LoggerFactory.getLogger(clz);
        this.name = name;
        this.requestsPerLog = requestsPerLog;
        this.reqId = lastRequestId.getAndIncrement();
    }

    public void logStartBlocking() {
        logStart("blocking");
    }

    public void logStartNonBlocking() {
        logStart("non-blocking");
    }

    public void logStartProcessingStepBlocking(int processingStepNo) {
        logStartProcessingStep("blocking", processingStepNo);
    }
    public void logStartProcessingStepNonBlocking(int processingStepNo) {
        logStartProcessingStep("non-blocking", processingStepNo);
    }

    public void logAsynchProcessingStepComplete() {

        long concReqs = concurrentRequests.get();

        updateStatistics(reqId, concReqs);

        log.debug("{}: Asynch call complete för request #{}, hand over to the state machine for the next action", concReqs, reqId);
    }

    public void logMessage(String message) {
        long concReqs = concurrentRequests.get();

        log.debug("{}: Request #{} - {}", concReqs, reqId, message);
    }

    public void logEndProcessingStepBlocking(int processingStepNo, int httpStatus) {
        logEndProcessingStep("blocking", processingStepNo, httpStatus);
    }

    public void logEndProcessingStepNonBlocking(int processingStepNo, int httpStatus) {
        logEndProcessingStep("non-blocking", processingStepNo, httpStatus);
    }

    public void logEndBlocking(int httpStatus) {
        logEnd("blocking", httpStatus, null);
    }

    public void logEndNonBlocking(int httpStatus, boolean deferredStatus) {
        logEnd("non-blocking", httpStatus, deferredStatus);
    }

    public void logExceptionBlocking(Throwable t) {
        logException("blocking", t);
    }

    public void logExceptionNonBlocking(Throwable t) {
        logException("non-blocking", t);
    }

    public void logLeaveThreadNonBlocking() {

        long concReqs = concurrentRequests.get();

        log.debug("{}: Processing of non-blocking {} request #{}, leave the request thread", concReqs, name, reqId);
    }

    public void logAlreadyExpiredNonBlocking() {

        long concReqs = concurrentRequests.getAndDecrement();

        log.warn("{}: Processing of non-blocking {} request #{} already expired", concReqs, name, reqId);
    }

    /*
     * PRIVATE PARTS :-)
     */

    protected void logStart(String type) {

        long concReqs = concurrentRequests.getAndIncrement();

        updateStatistics(reqId, concReqs);

        log.debug("{}: Start of {} {} request #{}.", concReqs, type, name, reqId);
    }

    protected void logEnd(String type, int httpStatus, Boolean deferredStatus) {

        long concReqs = concurrentRequests.getAndDecrement();

        if (deferredStatus == null) {
            log.debug("{}: End of {} {} request #{}, http-status: {}", concReqs, type, name, reqId, httpStatus);
        } else {
            log.debug("{}: End of {} {} request #{}, http-status: {}, deferred-status: {}", concReqs, type, name, reqId, httpStatus, deferredStatus);
        }
    }

    protected void logStartProcessingStep(String type, int processingStepNo) {

        long concReqs = concurrentRequests.getAndIncrement();

        log.debug("{}: Start processing of {} call #{} in request #{}", concReqs, type, processingStepNo, reqId);
    }

    protected void logEndProcessingStep(String type, int processingStepNo, int httpStatus) {

        long concReqs = concurrentRequests.getAndDecrement();

        log.debug("{}: End of processing of {} call #{} in request #{}, http-status: {}", concReqs, type, processingStepNo, reqId, httpStatus);
    }

    protected void logException(String type, Throwable t) {

        long concReqs = concurrentRequests.getAndDecrement();

        log.warn("{}: Processing of {} {} request #{} caused an exception: {}", concReqs, type, name, reqId, t);
    }
    protected void updateStatistics(long reqId, long concReqs) {
        if (concReqs > maxConcurrentRequests) {
            maxConcurrentRequests = concReqs;
        }

        if (reqId % requestsPerLog == 0 && reqId > 0) {
            Object openFiles = "UNKNOWN";
            if (os instanceof UnixOperatingSystemMXBean) {
                openFiles = ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount();
            }
            log.info("Statistics: noOfReqs: {}, maxConcReqs: {}, openFiles: {}", reqId, maxConcurrentRequests, openFiles);
        }
    }

}
