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
    private String url;

    public DbLookupRunnable(int dbLookupMs, int dbHits, String url, int minMs, int maxMs, DeferredResult<String> deferredResult) {
        this.dbLookupMs = dbLookupMs;
        this.dbHits = dbHits;
        this.url = url;
        this.minMs = minMs;
        this.maxMs = maxMs;
		this.deferredResult = deferredResult;
	}

	@Override
	public void run() {
        //seconds later in another thread...
        int noOfCalls = execute();
        AggregatorEventHandler aeh = new AggregatorEventHandler(noOfCalls, url, minMs, maxMs, deferredResult);
        aeh.onStart();
	}

	public int execute() {

        LOG.debug("Start of blocking dbLookup");
		int hits = simulateDbLookup();
        LOG.debug("Processing of blocking dbLookup done");

		return hits;
	}
	
	protected int simulateDbLookup(){

        try {
            Thread.sleep(dbLookupMs);
        } catch (InterruptedException e) {}

        return dbHits;
    }
}