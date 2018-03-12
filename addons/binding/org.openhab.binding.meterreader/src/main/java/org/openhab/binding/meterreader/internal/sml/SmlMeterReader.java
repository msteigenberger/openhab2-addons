/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal.sml;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.openhab.binding.meterreader.connectors.IMeterReaderConnector;
import org.openhab.binding.meterreader.internal.MeterDevice;
import org.openhab.binding.meterreader.internal.MeterValue;
import org.openhab.binding.meterreader.internal.helper.ProtocolMode;
import org.openmuc.jsml.structures.EMessageBody;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlList;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.structures.SmlStatus;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a SML capable device.
 *
 * @author Mathias Gilhuber
 * @since 1.7.0
 */
public final class SmlMeterReader extends MeterDevice<SmlFile> {

    protected final Logger logger = LoggerFactory.getLogger(SmlMeterReader.class);

    /**
     * Static factory method to create a SmlDevice object with a serial connector member.
     *
     * @param deviceId the id of the device as defined in openHAB configuration.
     * @param pullRequestRequired identicates if SML values have to be actively requested.
     * @param serialPort the port where the device is connected as defined in openHAB configuration.
     * @param serialParameter
     * @param initMessage
     */
    public static SmlMeterReader createInstance(String deviceId, String serialPort, byte[] initMessage, int baudrate,
            int baudrateChangeDelay) {
        SmlMeterReader device = new SmlMeterReader(deviceId, serialPort, initMessage, baudrate, baudrateChangeDelay,
                ProtocolMode.SML);

        return device;
    }

    /**
     * Constructor to create a SmlDevice object with a serial connector member.
     *
     * @param deviceId the id of the device as defined in openHAB configuration.
     * @param serialPort the port where the device is connected as defined in openHAB configuration.
     * @param serialParameter
     * @param initMessage
     * @param baudrate
     */
    private SmlMeterReader(String deviceId, String serialPort, byte[] initMessage, int baudrate,
            int baudrateChangeDelay, ProtocolMode protocolMode) {
        super(deviceId, serialPort, initMessage, baudrate, baudrateChangeDelay, protocolMode);

        logger.debug("Created SmlDevice instance {} with serial connector on port {}", deviceId, serialPort);
    }

    /**
     * Decodes native SML informations from the device and stores them locally until the next read request.
     *
     * @param smlFile the native SML informations from the device
     */
    @Override
    protected void populateValueCache(SmlFile smlFile) {
        if (smlFile != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Read out following SML file: {}", System.lineSeparator());
                SmlFileDebugOutput.printFile(smlFile, (msg) -> logger.trace(msg));
            }
            List<SmlMessage> smlMessages = smlFile.getMessages();

            if (smlMessages != null) {
                int messageCount = smlMessages.size();

                if (messageCount <= 0) {
                    logger.warn("{}: no valid SML messages list retrieved.", this.toString());
                }

                for (int i = 0; i < messageCount; i++) {
                    SmlMessage smlMessage = smlMessages.get(i);

                    if (smlMessage == null) {
                        logger.warn("{}: no valid SML message.", this.toString());
                        continue;
                    }

                    int tag = smlMessage.getMessageBody().getTag().id();

                    if (tag != EMessageBody.GET_LIST_RESPONSE.id()) {
                        continue;
                    }

                    SmlGetListRes listResponse = (SmlGetListRes) smlMessage.getMessageBody().getChoice();
                    SmlList smlValueList = listResponse.getValList();
                    SmlListEntry[] smlListEntries = smlValueList.getValListEntry();

                    for (SmlListEntry entry : smlListEntries) {
                        SmlStatus status = entry.getStatus();
                        if (status != null) {
                            readStatus(status);
                        }
                        SmlValueExtractor valueExtractor = new SmlValueExtractor(entry);
                        String obis = valueExtractor.getObisCode();

                        MeterValue smlValue = getSmlValue(obis);

                        if (smlValue == null) {
                            smlValue = valueExtractor.getSmlValue();
                        }

                        addObisCache(smlValue);
                    }
                }

            } else {
                logger.warn("{}: no valid SML messages list retrieved.", this.toString());
            }
        } else {
            logger.warn("{}: no valid SML File.", this.toString());
        }
    }

    private void readStatus(SmlStatus status) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            status.getChoice().encode(new DataOutputStream(byteArrayOutputStream));
            byte[] statusBytes = byteArrayOutputStream.toByteArray();
            if (statusBytes.length > 0) {

                Direction direction = getDirection(statusBytes);
                setEnergyDirection(direction);
            }
        } catch (IOException e) {
            logger.error("Failed to read status bytes", e);
        }
    }

    /**
     * Get the direction according to FNN Lastenheft 7.1.2 EDL
     *
     * @see https://www.vde.com/resource/blob/951000/252eb3cdf1c7f6cdea10847be399da0d/fnn-lastenheft-edl-1-0-2010-01-13-data.pdf
     * @param statusBytes
     * @return
     */
    private static Direction getDirection(byte... statusBytes) {
        // Bit 5 indicates the direction (Einspeisung/Bezug)
        // Bezug: 1000 0010
        // Einspeisung: 1010 0010
        switch (statusBytes[0] >> 5 & 0x01) {
            case 1:
                return Direction.MINUS;
            case 0:
                return Direction.PLUS;
        }
        return Direction.PLUS;
    }

    @Override
    protected IMeterReaderConnector<SmlFile> createConnector(String serialPort, int baudrate, int baudrateChangeDelay,
            ProtocolMode protocolMode) {
        return new SmlSerialConnector(serialPort, baudrate, baudrateChangeDelay);
    }

    @Override
    protected void printInfo() {
        super.printInfo();
    }
}
