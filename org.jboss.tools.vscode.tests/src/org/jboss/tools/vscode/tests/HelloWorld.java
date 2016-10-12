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
package org.jboss.tools.vscode.tests;

import static org.junit.Assert.assertNotNull;

import org.jboss.tools.langs.InitializeParams;
import org.jboss.tools.langs.base.LSPMethods;
import org.jboss.tools.langs.base.RequestMessage;
import org.jboss.tools.langs.base.ResponseMessage;
import org.jboss.tools.vscode.tests.lspclient.TestLSPClient;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author	Gorkem Ercan
 *
 */
public class HelloWorld {

	private static TestLSPClient client;

	@BeforeClass
	public static void initClient(){
		Thread t = new Thread(()->{
			client = new TestLSPClient();
			client.start();
		});
		t.start();
		while(client == null ){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	@Test
	public void A_testInit(){
		RequestMessage<InitializeParams> message = new RequestMessage<>();
		message.setMethod(LSPMethods.INITIALIZE.getMethod());
		message.setId(1);
		message.setParams(new InitializeParams().withRootPath(""));
		ResponseMessage<InitializeParams> response= (ResponseMessage<InitializeParams>) client.sendAndWait(message);
		assertNotNull(response.getResult());

	}


}
