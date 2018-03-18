/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.meterreader.MeterReaderBindingConstants;

/**
 *
 * @author MatthiasS
 *
 */
public class NegateBitParser {

    /**
     * "1-0:1-8-0:5:1"
     *
     * @param negateProperty
     * @return
     */
    public static NegateBitModel parseNegateProperty(String negateProperty) throws IllegalArgumentException {
        Pattern obisPattern = Pattern.compile(MeterReaderBindingConstants.OBIS_PATTERN_CHANNELID);
        try {
            Matcher matcher = obisPattern.matcher(negateProperty);
            if (matcher.find()) {
                String obis = matcher.group();
                String substring = negateProperty.substring(matcher.end() + 1, negateProperty.length());
                String[] split = substring.split(":");
                int negatePosition = Integer.parseInt(split[0]);
                boolean negateBit = Integer.parseInt(split[1]) == 0 ? false : true;
                boolean status = split.length > 2 ? split[2].equalsIgnoreCase("status") : false;
                return new NegateBitModel((byte) negatePosition, negateBit, obis, status);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Negate property cannot be parsed: " + negateProperty, e);
        }
        throw new IllegalArgumentException("Negate property cannot be parsed: " + negateProperty);
    }

    protected String getObisChannelId(String obis) {
        return obis.replaceAll("\\.", "-").replaceAll(":|\\*", "#");
    }
}
