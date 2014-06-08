package se.callista.springmvc.asynch.pattern.aggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

public class DbLookupRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DbLookupRunnable.class);

    private int dbLookupMs;
    private int dbHits;
    private final int maxMs;
    private final int minMs;
    private DeferredResult<String> deferredResult;

    public DbLookupRunnable(int dbLookupMs, int dbHits, int minMs, int maxMs, DeferredResult<String> deferredResult) {
        this.dbLookupMs = dbLookupMs;
        this.dbHits = dbHits;
        this.minMs = minMs;
        this.maxMs = maxMs;
		this.deferredResult = deferredResult;
	}

	@Override
	public void run() {
        //seconds later in another thread...
        int noOfCalls = execute();
        new AggregatorEventHandler(noOfCalls, minMs, maxMs, deferredResult);
	}

	public int execute() {
		int hits = simulateDbLookup();
		return hits;
	}
	
	protected int simulateDbLookup(){

        LOG.debug("Start of blocking dbLookup");

        try {
            Thread.sleep(dbLookupMs);
        } catch (InterruptedException e) {}

        LOG.debug("Processing of blocking dbLookup done");

        return dbHits;
    }
}