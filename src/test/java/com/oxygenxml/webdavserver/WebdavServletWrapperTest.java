package com.oxygenxml.webdavserver;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class WebdavServletWrapperTest {
  
  /**
   * <p><b>Description:</b> Test that we correctly identify requests that want only the resource type.</p>
   * <p><b>Bug ID:</b> WA-2211</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testWebDAVRequestParsingNotWellFormed() throws Exception {
    assertFalse(WebdavServletWrapper.isOnlyTypeRequested(new ByteArrayInputStream("<root>".getBytes())));
    String goodReqBody = "<?xml version=\"1.0\"?>\r\n" + 
        "<a:propfind xmlns:a=\"DAV:\">\r\n" + 
        "<a:prop><a:resourcetype/></a:prop>\r\n" + 
        "</a:propfind>";
    assertTrue(WebdavServletWrapper.isOnlyTypeRequested(new ByteArrayInputStream(goodReqBody.getBytes())));
    String badReqBody = "<?xml version=\"1.0\"?>\r\n" + 
        "<a:propfind xmlns:a=\"DAV:\">\r\n" + 
        "<a:prop><a:resourcetype/><a:lock/></a:prop>\r\n" + 
        "</a:propfind>";
    assertFalse(WebdavServletWrapper.isOnlyTypeRequested(new ByteArrayInputStream(badReqBody.getBytes())));
    String badReq2Body = "<?xml version=\"1.0\"?>\r\n" + 
        "<a:propfind xmlns:a=\"DAV:\">\r\n" + 
        "<a:prop><a:lock/></a:prop>\r\n" + 
        "</a:propfind>";
    assertFalse(WebdavServletWrapper.isOnlyTypeRequested(new ByteArrayInputStream(badReq2Body.getBytes())));
  }
}
