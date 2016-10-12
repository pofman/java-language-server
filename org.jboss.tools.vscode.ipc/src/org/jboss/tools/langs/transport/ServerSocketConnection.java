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
package org.jboss.tools.langs.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

import org.jboss.tools.vscode.internal.ipc.IPCPlugin;

public class ServerSocketConnection extends AbstractConnection {


	private class ReaderThread extends Thread{

		private InputStream stream;

		public ReaderThread() {
			super("LSP_ClientReaderThread");
		}

		@Override
		public void run() {
			try {
				readServer = new ServerSocket(readPort);
				Socket s =  readServer.accept();
				stream =  s.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (stream == null) {
				// TODO need proper error handling
				return;
			}
			while(true){
				TransportMessage message;
				try {
					message = TransportMessage.fromStream(stream, DEFAULT_CHARSET);
					if(message == null ){
						//Stream disconnected exit reader thread
						IPCPlugin.logError("Empty message read");
						break;
					}
					inboundQueue.add(message);
				} catch (IOException e) {
					//continue
				}
			}
		}
	}

	private class WriterThread extends Thread{

		private  OutputStream stream;

		public WriterThread() {
			super("LSP_ClientWriterThread");
		}

		@Override
		public void run() {
			try {
				writeServer = new ServerSocket(writePort);
				Socket s = writeServer.accept();
				stream = s.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (stream == null) {
				// TODO need proper error handling
				return;
			}
			while (true) {
				try {
					TransportMessage message = outboundQueue.take();
					message.send(stream, DEFAULT_CHARSET);
					stream.flush();
				} catch (InterruptedException e) {
					break;//exit
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private final int readPort;
	private final int writePort;

	private ServerSocket readServer;
	private ServerSocket writeServer;

	private Socket writeSocket;
	private Socket readSocket;

	public ServerSocketConnection(int readPort , int writePort) {
		this.readPort = readPort;
		this.writePort = writePort;
	}

	@Override
	public void start() throws IOException {
		ReaderThread readerThread = new ReaderThread();
		readerThread.setDaemon(true);
		readerThread.start();

		WriterThread writerThread = new WriterThread();
		writerThread.setDaemon(true);
		writerThread.start();
	}

	@Override
	public void close() {
		try {
			if(readSocket != null)
				readSocket.close();
			if(writeSocket != null )
				writeSocket.close();
			if(readServer != null)
				readServer.close();
			if(writeServer != null)
				writeServer.close();
		} catch (IOException e) {
			// unhandled
		}
	}

}
