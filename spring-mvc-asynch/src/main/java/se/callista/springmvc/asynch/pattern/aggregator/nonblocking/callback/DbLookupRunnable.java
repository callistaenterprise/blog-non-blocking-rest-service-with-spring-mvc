package se.callista.springmvc.asynch.pattern.aggregator.nonblocking.callback;

import org.springframework.web.context.request.async.DeferredResult;
import se.callista.springmvc.asynch.common.log.LogHelper;

public class DbLookupRunnable implements Runnable {

    private final LogHelper log;

    private int dbLookupMs;
    private int dbHits;
    private final int maxMs;
    private final int minMs;
    private final int timeoutMs;
    private DeferredResult<String> deferredResult;
    private String url;

    public DbLookupRunnable(LogHelper log, int dbLookupMs, int dbHits, String url, int minMs, int maxMs, int timeoutMs, DeferredResult<String> deferredResult) {
        this.log = log;
        this.dbLookupMs = dbLookupMs;
        this.dbHits = dbHits;
        this.url = url;
        this.minMs = minMs;
        this.maxMs = maxMs;
        this.timeoutMs = timeoutMs;
		this.deferredResult = deferredResult;
	}

	@Override
	public void run() {
        //seconds later in another thread...
        int noOfCalls = execute();
        AggregatorEventHandler aeh = new AggregatorEventHandler(log, noOfCalls, url, minMs, maxMs, timeoutMs, deferredResult);
        aeh.onStart();
	}

	public int execute() {

        log.logMessage("Start of blocking dbLookup");
		int hits = simulateDbLookup();
        log.logMessage("Processing of blocking dbLookup done");

		return hits;
	}
	
	protected int simulateDbLookup(){

        try {
            Thread.sleep(dbLookupMs);
        } catch (InterruptedException e) {}

        return dbHits;
    }
}