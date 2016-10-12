/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.langs.base;

import org.jboss.tools.langs.transport.Connection;
import org.jboss.tools.langs.transport.TransportMessage;

/**
 * Base client implementation
 *
 * @author Gorkem Ercan
 *
 */
public abstract class LSPClient {

	protected Connection connection;
	private final JSONHelper jsonHelper;

	public LSPClient(){
		jsonHelper = new JSONHelper();
	}

	public void send (Message message){
		TransportMessage tm = new TransportMessage(jsonHelper.toJson(message));
		connection.send(tm);
	}

	/**
	 * Parses the message client
	 *
	 * @param message
	 * @param msg
	 * @return
	 */
	protected Message maybeParseMessage(TransportMessage message) {
		return jsonHelper.fromJson(message);
	}

}
