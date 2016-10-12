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
package org.jboss.tools.vscode.tests.lspclient;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.tools.langs.base.LSPClient;
import org.jboss.tools.langs.base.LSPException;
import org.jboss.tools.langs.base.Message;
import org.jboss.tools.langs.base.NotificationMessage;
import org.jboss.tools.langs.base.RequestMessage;
import org.jboss.tools.langs.base.ResponseMessage;
import org.jboss.tools.langs.transport.Connection;
import org.jboss.tools.langs.transport.ServerSocketConnection;
import org.jboss.tools.langs.transport.TransportMessage;
import org.jboss.tools.vscode.java.internal.LanguageServer;

/**
 *
 * @author Gorkem Ercan
 *
 */
public class TestLSPClient extends LSPClient {

	protected final BlockingQueue<ResponseMessage<?>> responseQueue = new LinkedBlockingQueue<>();

	public void start(){

		try {
			this.connection = initConnection();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		startDispatching();
		LanguageServer server = new LanguageServer();
		try {
			server.start(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private Connection initConnection() throws IOException {
		final String rPort = System.getenv().get("STDIN_PORT");
		final String wPort = System.getenv().get("STDOUT_PORT");
		ServerSocketConnection conn = new ServerSocketConnection(Integer.parseInt(rPort),
				Integer.parseInt(wPort));
		conn.start();
		return conn;
	}

	private void startDispatching() {
		Thread t = new Thread(() -> {
			try {
				while (true) {
					TransportMessage message = connection.take();
					runMessage(message);
				}
			} catch (InterruptedException e) {
				//Exit dispatch
				e.printStackTrace();
			}
		});
		t.start();
	}


	public ResponseMessage<?> sendAndWait(RequestMessage<?> message){
		send(message);
		try {
			return responseQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void runMessage(final TransportMessage message ){
		Message msg = maybeParseMessage(message);
		if (msg == null)
			return;

		if (msg instanceof NotificationMessage) {
			NotificationMessage<?> nm = (NotificationMessage<?>) msg;
			try {
				System.out.println("Notification:" + message.getContent());
			} catch (LSPException e) {
				e.printStackTrace();
			}
		}

		if (msg instanceof ResponseMessage) {
			ResponseMessage<?> rm = (ResponseMessage<?>) msg;
			responseQueue.add(rm);
		}
	}



}
