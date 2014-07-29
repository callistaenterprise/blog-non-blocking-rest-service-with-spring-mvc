package se.callista.springmvc.asynch.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ProcessingStatus {

    @XmlElement
    private final String status;

    @XmlElement
    private final int processingTimeMs;

    public ProcessingStatus() {
        status = "UNKNOWN";
        processingTimeMs = -1;
    }
    
    public ProcessingStatus(String status, int processingTimeMs) {
        this.status = status;
        this.processingTimeMs = processingTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public int getProcessingTimeMs() {
        return processingTimeMs;
    }
}