/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.factory;

import java.net.URI;
import java.net.UnknownHostException;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.rfc2217.TelnetSerialPort;

/**
 *
 * @author MatthiasS
 *
 */
public class RFC2217PortCreator implements SerialPortCreator<TelnetSerialPort> {

    private final static String PROTOCOL = "rfc2217";

    @Override
    public boolean isApplicable(String portName, Class<TelnetSerialPort> expectedClass) {
        try {
            if (expectedClass.isAssignableFrom(TelnetSerialPort.class)) {
                URI uri = URI.create(portName);
                return uri.getScheme().equalsIgnoreCase(PROTOCOL);
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * @throws UnsupportedCommOperationException if connection to the remote serial port fails.
     * @throws NoSuchPortException if the host does not exist.
     */
    @Override
    public TelnetSerialPort createPort(String portName)
            throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException {
        URI url = URI.create(portName);
        try {
            TelnetSerialPort telnetSerialPort = new TelnetSerialPort();
            telnetSerialPort.getTelnetClient().connect(url.getHost(), url.getPort());
            return telnetSerialPort;
        } catch (UnknownHostException e) {
            throw new NoSuchPortException(/* "Host "+url.getHost()+" not available", e */);
        } catch (Exception e) {
            throw new UnsupportedCommOperationException(
                    "Unable to establish remote connection to serial port " + portName/* , e */);
        }
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

}
