/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader;

/**
 * The {@link MeterReaderConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Matthias Steigenberger- Initial contribution
 */
public class MeterReaderConfiguration {

    public String port;

    public Integer refresh;

    public Integer baudrateChangeDelay;

    public String initMessage;

    public String baudrate;

    public String mode;

    public String conformity;
}