/*
 * Copyright (C) 2015 - 2023. Henrik Bærbak Christensen, Aarhus University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cloud.cave.invoker;

import frds.broker.RequestObject;
import frds.broker.ipc.http.MimeMediaType;
import frds.broker.ipc.http.UriTunnelServerRequestHandler;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import static spark.Spark.*;

/** A URITunnel SRH that adds a few extra paths to the server
 * for inspection.
 */
public class CaveUriTunnelServerRequestHandler extends UriTunnelServerRequestHandler {
  private int requestCount = 0;
  // Like to keep the last requests and replies around for inspection
  private String lastRequest, lastReply;

  @Override
  public void start() {
    super.start();

    before((req, res) -> {
      String body = req.body();

      RequestObject p = gson.fromJson(body, RequestObject.class);
      if (p != null) lastRequest = p.toString();

      requestCount++;
    });

    after((req, res) -> {
      // Avoid the /info requests...
      if (res.type() != null)
        lastReply = res.body();
    });

    // GET can show some statistics
    get( "/info", (req, res) -> {
      String html;
      html = generateStatisticsPage();
      return html;
    });

  }

  private String generateStatisticsPage() {
    String html;
    html = "<h1>SkyCave Daemon HTTP Server</h1>";
    html += "<h2>Statistics</h2> <p>Requests handled during life time: "+requestCount+ "</p>";
    html += "<p> Last Request:<blockquote><code>" + lastRequest + "</blockquote></code></p>";
    html += "<p> Last Reply:<blockquote><code>" + lastReply + "</blockquote></code></p>";

    InetAddress ip = null;
    try {
      ip = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      logger.error("Why can I not get my host address?", e);
    }
    html += "<p>This node has IPs:"+ getAllIps() + "</p>";

    html += "<h2>Credits</h2> <p>SkyCave designed and implemented by Henrik Bærbak Christensen...</p>";
    return html;
  }

  // Thanks to
  // https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
  private String getAllIps() {
    Enumeration<NetworkInterface> e = null;
    String retval = "<ul>";
    try {
      e = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e1) {
      e1.printStackTrace();
    }
    while(e.hasMoreElements())
    {
      NetworkInterface n = (NetworkInterface) e.nextElement();
      Enumeration ee = n.getInetAddresses();
      while (ee.hasMoreElements())
      {
        InetAddress i = (InetAddress) ee.nextElement();
        // Filter out ipv6 addresses
        String ip = i.getHostAddress();
        if ( ! ip.contains("%"))
          retval += "<li>" + ip + "</li>";
      }
    }
    retval += "</ul>";
    return retval;
  }

}
