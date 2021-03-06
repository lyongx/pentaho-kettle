/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.websocket.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.events.LogEvent;
import org.pentaho.di.engine.api.reporting.LogEntry;
import org.pentaho.di.engine.api.reporting.LogLevel;
import org.pentaho.di.engine.model.Operation;
import org.pentaho.di.trans.ael.websocket.event.MessageEvent;
import org.pentaho.di.trans.ael.websocket.event.MessageEventType;
import org.pentaho.di.trans.ael.websocket.exception.HandlerRegistrationException;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventFireEventException;
import org.pentaho.di.trans.ael.websocket.handler.MessageEventHandler;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.pentaho.di.core.util.Assert.assertFalse;
import static org.pentaho.di.core.util.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith( MockitoJUnitRunner.class )
public class MessageEventServiceImplTest {

  @Mock private MessageEvent transformationMessageEvent;
  @Mock private MessageEvent operationMessageEvent;
  @Mock private MessageEvent otherOpMessageEvent;
  @Mock private MessageEvent otherTransMessageEvent;
  @Mock private MessageEventHandler messageEventHandler;
  @Mock private MessageEventHandler messageEventHandler2;
  @Mock private LogEntry logEntry;
  private MessageEventServiceImpl messageEventService;

  @Before
  public void before() {
    messageEventService = new MessageEventServiceImpl();
    doReturn( "Handler_ID" ).when( messageEventHandler ).getIdentifier();
    doReturn( "Handler_ID2" ).when( messageEventHandler2 ).getIdentifier();

    doReturn( MessageEventType.TRANSFORMATION_LOG ).when( transformationMessageEvent ).getType();
    doReturn( MessageEventType.OPERATION_LOG ).when( operationMessageEvent ).getType();
    doReturn( "Operation_ID" ).when( operationMessageEvent ).getObjectId();
    doReturn( MessageEventType.METRICS ).when( otherOpMessageEvent ).getType();
    doReturn( "Metrics_ID" ).when( otherOpMessageEvent ).getObjectId();
    doReturn( MessageEventType.TRANSFORMATION_STATUS ).when( otherOpMessageEvent ).getType();

    doReturn( "message" ).when( logEntry ).getMessage();
    doReturn( LogLevel.ERROR ).when( logEntry ).getLogLogLevel();
    doReturn( new Date() ).when( logEntry ).getTimestamp();
  }

  @Test
  public void testTranformationAddHandler() throws KettleException {
    messageEventService.addHandler( transformationMessageEvent, messageEventHandler );

    assertTrue( messageEventService.getHandlersFor( transformationMessageEvent ).size() == 1 );
    assertTrue( messageEventHandler.getIdentifier()
      .equals( messageEventService.getHandlersFor( transformationMessageEvent ).get( 0 ).getIdentifier() ) );
  }

  @Test( expected = HandlerRegistrationException.class )
  public void testTranformationDuplicateAddHandler() throws KettleException {
    messageEventService.addHandler( transformationMessageEvent, messageEventHandler );
    messageEventService.addHandler( transformationMessageEvent, messageEventHandler );
  }

  @Test
  public void testTranformationAddDiffHandlersForSameEvent() throws KettleException {
    testAddDiffHandlersForSameEvent( transformationMessageEvent, messageEventHandler, messageEventHandler2 );
  }

  @Test
  public void testTransformationHasHandler() throws KettleException {
    addHandlers( transformationMessageEvent, messageEventHandler, messageEventHandler2 );
    assertTrue( messageEventService.hasHandlers( transformationMessageEvent ) );
  }

  @Test
  public void testTransformationHasHandlerFalseTrans() throws KettleException {
    addHandlers( transformationMessageEvent, messageEventHandler, messageEventHandler2 );
    assertFalse( messageEventService.hasHandlers( otherTransMessageEvent ) );
  }

  @Test
  public void testTransformationHasHandlerFalseOp() throws KettleException {
    addHandlers( transformationMessageEvent, messageEventHandler, messageEventHandler2 );
    assertFalse( messageEventService.hasHandlers( otherOpMessageEvent ) );
  }

  @Test
  public void testOperationAddHandler() throws KettleException {
    messageEventService.addHandler( operationMessageEvent, messageEventHandler );

    assertTrue( messageEventService.getHandlersFor( operationMessageEvent ).size() == 1 );
    assertTrue( messageEventHandler.getIdentifier()
      .equals( messageEventService.getHandlersFor( operationMessageEvent ).get( 0 ).getIdentifier() ) );
  }

  @Test( expected = HandlerRegistrationException.class )
  public void testOperationDuplicateAddHandler() throws KettleException {
    messageEventService.addHandler( operationMessageEvent, messageEventHandler );
    messageEventService.addHandler( operationMessageEvent, messageEventHandler );
  }

  @Test
  public void testOperationAddDiffHandlersForSameEvent() throws KettleException {
    testAddDiffHandlersForSameEvent( operationMessageEvent, messageEventHandler, messageEventHandler2 );
  }

  @Test
  public void testOperationHasHandler() throws KettleException {
    addHandlers( operationMessageEvent, messageEventHandler, messageEventHandler2 );
    assertTrue( messageEventService.hasHandlers( operationMessageEvent ) );
  }

  @Test
  public void testOperationHasHandlerFalseTrans() throws KettleException {
    addHandlers( operationMessageEvent, messageEventHandler, messageEventHandler2 );
    assertFalse( messageEventService.hasHandlers( otherTransMessageEvent ) );
  }

  @Test
  public void testOperationHasHandlerFalseOp() throws KettleException {
    addHandlers( operationMessageEvent, messageEventHandler, messageEventHandler2 );
    assertFalse( messageEventService.hasHandlers( otherOpMessageEvent ) );
  }

  @Test( expected = HandlerRegistrationException.class )
  public void testMsgEventTypeNull() throws KettleException {
    messageEventService.addHandler( null, messageEventHandler );
  }

  @Test( expected = HandlerRegistrationException.class )
  public void testMsgHandlerNull() throws KettleException {
    messageEventService.addHandler( operationMessageEvent, null );
  }

  @Test( expected = HandlerRegistrationException.class )
  public void testbothNull() throws KettleException {
    messageEventService.addHandler( null, null );
  }

  @Test( expected = MessageEventFireEventException.class )
  public void testFireEventNull() throws KettleException {
    messageEventService.fireEvent( null );
  }

  @Test
  public void testOperationFireEvent() throws KettleException {
    addHandlers( operationMessageEvent, messageEventHandler, messageEventHandler2 );
    doReturn( true ).when( messageEventHandler ).isInterested( any( MessageEvent.class ) );
    doReturn( true ).when( messageEventHandler2 ).isInterested( any( MessageEvent.class ) );

    LogEvent logEvent = new LogEvent<>( new Operation( "Operation_ID", null ), logEntry );
    messageEventService.fireEvent( logEvent );
    verify( messageEventHandler ).execute( logEvent );
    verify( messageEventHandler2 ).execute( logEvent );
  }

  @Test
  public void testOperationFireEventNotInterested() throws KettleException {
    addHandlers( operationMessageEvent, messageEventHandler, messageEventHandler2 );
    doReturn( true ).when( messageEventHandler ).isInterested( any( MessageEvent.class ) );
    doReturn( false ).when( messageEventHandler2 ).isInterested( any( MessageEvent.class ) );

    LogEvent logEvent = new LogEvent<>( new Operation( "Operation_ID", null ), logEntry );
    messageEventService.fireEvent( logEvent );
    verify( messageEventHandler ).execute( logEvent );
    verify( messageEventHandler2, never() ).execute( logEvent );
  }

  @Test( expected = MessageEventFireEventException.class )
  public void testOperationFireEventThrowException() throws KettleException {
    addHandlers( operationMessageEvent, messageEventHandler, messageEventHandler2 );
    doThrow( new RuntimeException( "Test" ) ).when( messageEventHandler ).isInterested( any( MessageEvent.class ) );
    doReturn( true ).when( messageEventHandler2 ).isInterested( any( MessageEvent.class ) );

    LogEvent logEvent = new LogEvent<>( new Operation( "Operation_ID", null ), logEntry );
    messageEventService.fireEvent( logEvent );
    verify( messageEventHandler, never() ).execute( logEvent );
    verify( messageEventHandler2 ).execute( logEvent );
  }

  public void addHandlers( MessageEvent messageEvent, MessageEventHandler handler,
                           MessageEventHandler handler2 ) throws KettleException {
    messageEventService.addHandler( messageEvent, handler );
    messageEventService.addHandler( messageEvent, handler2 );
  }

  private void testAddDiffHandlersForSameEvent( MessageEvent messageEvent, MessageEventHandler handler,
                                                MessageEventHandler handler2 ) throws KettleException {
    messageEventService.addHandler( messageEvent, handler );
    messageEventService.addHandler( messageEvent, handler2 );

    assertTrue( messageEventService.getHandlersFor( messageEvent ).size() == 2 );
    assertTrue( handler.getIdentifier()
      .equals( messageEventService.getHandlersFor( messageEvent ).get( 0 ).getIdentifier() ) );
    assertTrue( handler2.getIdentifier()
      .equals( messageEventService.getHandlersFor( messageEvent ).get( 1 ).getIdentifier() ) );
  }
}
