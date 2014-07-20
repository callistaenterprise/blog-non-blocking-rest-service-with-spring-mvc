package se.callista.springmvc.asynch.pattern.aggregator.nonblocking.lambda;

import se.callista.springmvc.asynch.common.log.LogHelper;

/**
 * Created by magnus on 20/07/14.
 */
public class DbLookup {
    private final LogHelper LOG;
    private final int dbLookupMs;
    private final int dbHits;

    public DbLookup(LogHelper log, int dbLookupMs, int dbHits) {
        LOG = log;
        this.dbLookupMs = dbLookupMs;
        this.dbHits = dbHits;
    }

    public int executeDbLookup() {

        LOG.logMessage("Start of blocking dbLookup");
        int hits = simulateDbLookup();
        LOG.logMessage("Processing of blocking dbLookup done");

        return hits;
    }

    // Simulate a blocking db-lookup by putting the current thread to sleep for a while...
    protected int simulateDbLookup(){

        try {
            Thread.sleep(dbLookupMs);
        } catch (InterruptedException e) {}

        return dbHits;
    }
}
