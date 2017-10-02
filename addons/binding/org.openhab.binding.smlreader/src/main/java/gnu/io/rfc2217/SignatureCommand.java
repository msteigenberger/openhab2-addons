/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.rfc2217;

import static gnu.io.rfc2217.RFC2217.*;

import java.io.UnsupportedEncodingException;

/**
 * RFC 2217 {@code SIGNATURE} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 * @author jserv
 */
public class SignatureCommand extends ComPortCommand {

    public static final String ENCODING = "ISO-8859-1";

    private final String signature;
    private boolean decodeFailed;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *            NullPointerException if {@code bytes} is null
     *            IllegalArgumentException if {@code bytes} has length that is too short or too long
     *            IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *            IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#SIGNATURE} (client or server)
     */
    public SignatureCommand(int[] bytes) {
        super("SIGNATURE", SIGNATURE, bytes);
        String sig;
        boolean failed = false;
        try {
            sig = new String(this.getPayload(), ENCODING);
        } catch (UnsupportedEncodingException e) {
            sig = "(string decode failed)";
            failed = true;
        }
        this.signature = sig;
        this.decodeFailed = failed;
    }

    /**
     * Encoding constructor.
     *
     * @param signature signature string
     * @param client true for the client-to-server command, false for the server-to-client command
     */
    public SignatureCommand(boolean client, String signature) {
        this(encode(client, signature));
    }

    /**
     * Encoding constructor for signature requests.
     *
     * @param client true for the client command, false for the server command
     */
    public SignatureCommand(boolean client) {
        this(encode(client, ""));
    }

    @Override
    public String toString() {
        return this.getName() + " " + (this.signature.length() > 0 ? "\"" + this.signature + "\"" : "REQUEST");
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.caseSignature(this);
    }

    public String getSignature() {
        return this.signature;
    }

    public boolean decodeFailed() {
        return this.decodeFailed;
    }

    @Override
    int getMinPayloadLength() {
        return 0;
    }

    @Override
    int getMaxPayloadLength() {
        return Integer.MAX_VALUE - 2;
    }

    private static int[] encode(boolean client, String signature) {
        byte[] buf;
        try {
            buf = signature.getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            buf = new byte[] { (byte) '?' };
        }
        int[] ibuf = new int[2 + buf.length];
        ibuf[0] = COM_PORT_OPTION;
        ibuf[1] = client ? SIGNATURE : SIGNATURE + SERVER_OFFSET;
        for (int i = 0; i < buf.length; i++) {
            ibuf[2 + i] = buf[i] & 0xff;
        }
        return ibuf;
    }
}
