/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.meterreader.MeterReaderBindingConstants;

/**
 *
 * @author MatthiasS
 *
 */
public class ObisCode {
    /**
     * For more information see https://de.wikipedia.org/wiki/OBIS-Kennzahlen
     */
    public static final String OBIS_PATTERN = "((?<A>[0-9]{1,3})-(?<B>[0-9]{1,3}):)?(?<C>[0-9]{1,3}).(?<D>[0-9]{1,3}).(?<E>[0-9]{1,3})(\\*(?<F>[0-9][0-9]{1,3}))?";

    private static Pattern obisPattern = Pattern.compile(OBIS_PATTERN);

    private Byte a, b, c, d, e, f;

    private ObisCode(Byte a, Byte b, Byte c, Byte d, Byte e, Byte f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    public static ObisCode from(String obis) throws IllegalArgumentException {
        try {
            Matcher matcher = obisPattern.matcher(obis);
            if (matcher.find()) {
                String a = matcher.group("A");
                String b = matcher.group("B");
                String c = matcher.group("C");
                String d = matcher.group("D");
                String e = matcher.group("E");
                String f = matcher.group("F");
                return new ObisCode(a != null && !a.isEmpty() ? (byte) (0xFF & Integer.valueOf(a)) : null,
                        b != null && !b.isEmpty() ? (byte) (0xFF & Integer.valueOf(b)) : null,
                        (byte) (0xFF & Integer.valueOf(c)), (byte) (0xFF & Integer.valueOf(d)),
                        (byte) (0xFF & Integer.valueOf(e)),
                        f != null && !f.isEmpty() ? (byte) (0xFF & Integer.valueOf(f)) : null);
            }
            throw new IllegalArgumentException(obis + " is not correctly formated.");
        } catch (Exception e) {
            throw new IllegalArgumentException(obis + " is not correctly formated.", e);
        }
    }

    public String asDecimalString() {
        try (Formatter format = new Formatter()) {
            format.format(MeterReaderBindingConstants.OBIS_FORMAT, a != null ? a & 0xFF : 0, b != null ? b & 0xFF : 0,
                    c & 0xFF, d & 0xFF, e & 0xFF, f != null ? f & 0xFF : 0);
            return format.toString();
        }
    }

    public byte getAGroup() {
        return a;
    }

    public byte getBGroup() {
        return b;
    }

    public byte getCGroup() {
        return c;
    }

    public byte getDGroup() {
        return d;
    }

    public byte getEGroup() {
        return e;
    }

    public byte getFGroup() {
        return f;
    }

    @Override
    public String toString() {
        return asDecimalString();
    }

    public boolean matches(Byte a, Byte b, Byte c, Byte d, Byte e, Byte f) {
        return (this.a == null || a == null || this.a.equals(a)) && (this.b == null || b == null || this.b.equals(b))
                && this.c.equals(c) && this.d.equals(d) && this.e.equals(e)
                && (this.f == null || f == null || this.f.equals(f));
    }

    public boolean matches(Byte c, Byte d, Byte e) {
        return matches(null, null, c, d, e, null);
    }

}
