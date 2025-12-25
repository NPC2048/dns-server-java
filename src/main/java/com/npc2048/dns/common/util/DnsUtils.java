package com.npc2048.dns.common.util;

import com.npc2048.dns.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;

import java.util.List;

/**
 * @author yuelong.liang
 */
@Slf4j
public class DnsUtils {

    private DnsUtils() {
    }

    /**
     * Extract TTL value
     */
    public static int extractTtl(byte[] responseData) {
        try {
            Message response = new Message(responseData);
            List<Record> answers = response.getSection(Section.ANSWER);

            if (answers != null && !answers.isEmpty()) {
                return (int) answers.getFirst().getTTL();
            }
            // Default 5 minutes
            return Constants.CACHE_DEFAULT_TTL;
        } catch (Exception e) {
            log.warn("Failed to extract TTL, using default value", e);
            return Constants.CACHE_DEFAULT_TTL;
        }
    }

}
