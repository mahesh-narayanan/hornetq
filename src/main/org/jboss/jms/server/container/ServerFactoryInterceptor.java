/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jms.server.container;

import org.jboss.aop.Interceptor;
import org.jboss.aop.Invocation;
import org.jboss.aop.MethodInvocation;
import org.jboss.aop.SimpleMetaData;
import org.jboss.jms.client.BrowserDelegate;
import org.jboss.jms.client.ConnectionDelegate;
import org.jboss.jms.client.ConsumerDelegate;
import org.jboss.jms.client.ProducerDelegate;
import org.jboss.jms.client.SessionDelegate;
import org.jboss.jms.container.Container;

/**
 * The interceptor to create server containers
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision$
 */
public class ServerFactoryInterceptor
   implements Interceptor
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static final ServerFactoryInterceptor singleton = new ServerFactoryInterceptor();

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Interceptor implementation -----------------------------------

   public String getName()
   {
      return "ServerFactoryInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      Object result = invocation.invokeNext();
      
      MethodInvocation mi = (MethodInvocation) invocation;
      String methodName = mi.method.getName();
      if (methodName.equals("createSession"))
         return createSession(mi, (SessionDelegate) result);
      else if (methodName.equals("createBrowser"))
         return createBrowser(mi, (BrowserDelegate) result);
      else if (methodName.equals("createConsumer"))
         return createConsumer(mi, (ConsumerDelegate) result);
      else if (methodName.equals("createProducer"))
         return createProducer(mi, (ProducerDelegate) result);
      else
         return result;
   }

   // Protected ------------------------------------------------------
   
   protected SessionDelegate createSession(MethodInvocation invocation, SessionDelegate target)
      throws Throwable
   {
      Client client = Client.getClient(invocation);
      SimpleMetaData metaData = client.createSession(invocation);
      Interceptor[] interceptors = new Interceptor[]
      {
         singleton,
         ServerSessionInterceptor.singleton 
      };
      ConnectionDelegate connection = (ConnectionDelegate) Container.getProxy(invocation); 
      return ServerContainerFactory.getSessionContainer(connection, interceptors, metaData);
   }
   
   protected BrowserDelegate createBrowser(MethodInvocation invocation, BrowserDelegate target)
      throws Throwable
   {
      Client client = Client.getClient(invocation);
      SimpleMetaData metaData = client.createBrowser(invocation);
      Interceptor[] interceptors = new Interceptor[]
      {
         ServerBrowserInterceptor.singleton 
      };
      SessionDelegate session = (SessionDelegate) Container.getProxy(invocation); 
      return ServerContainerFactory.getBrowserContainer(session, interceptors, metaData);
   }
   
   protected ConsumerDelegate createConsumer(MethodInvocation invocation, ConsumerDelegate target)
      throws Throwable
   {
      Client client = Client.getClient(invocation);
      SimpleMetaData metaData = client.createConsumer(invocation);
      Interceptor[] interceptors = new Interceptor[]
      {
         ServerConsumerInterceptor.singleton 
      };
      SessionDelegate session = (SessionDelegate) Container.getProxy(invocation); 
      return ServerContainerFactory.getConsumerContainer(session, interceptors, metaData);
   }
   
   protected ProducerDelegate createProducer(MethodInvocation invocation, ProducerDelegate target)
      throws Throwable
   {
      Client client = Client.getClient(invocation);
      SimpleMetaData metaData = client.createProducer(invocation);
      Interceptor[] interceptors = new Interceptor[]
      {
         ServerProducerInterceptor.singleton 
      };
      SessionDelegate session = (SessionDelegate) Container.getProxy(invocation); 
      return ServerContainerFactory.getProducerContainer(session, interceptors, metaData);
   }

   // Package Private ------------------------------------------------

   // Private --------------------------------------------------------

   // Inner Classes --------------------------------------------------

}
