package org.springframework.cloud.gcp;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gcp.storage.GoogleStorageResource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Vinicius Carvalho
 */
@SpringBootTest()
@RunWith(SpringRunner.class)
public class StorageAutoConfigurationTests {



	@Value("gs://test-spring/images/spring.png")
	private Resource remoteResource;



	@Test
	public void testValidObject() throws Exception{
		Assert.assertTrue(remoteResource.exists());
		Assert.assertEquals(4096L, remoteResource.contentLength());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = remoteResource.getInputStream();
		byte[] data = new byte[(int) remoteResource.contentLength()];
		int read = 0;
		while ((read = is.read(data, 0, data.length)) != -1) {
			baos.write(data, 0, read);
		}
		baos.flush();
	}

	@SpringBootApplication
	static class StorageApplication{
		public static void main(String[] args) {
			SpringApplication.run(StorageApplication.class,args);
		}
		@Bean
		public Storage mockStorage() throws Exception{
			Storage storage = Mockito.mock(Storage.class);
			
			BlobId validBlob = BlobId.of("test-spring","images/spring.png");
			Blob mockedBlob = Mockito.mock(Blob.class);
			Mockito.when(mockedBlob.exists()).thenReturn(true);
			Mockito.when(mockedBlob.getSize()).thenReturn(4096L);
			ByteArrayInputStream bin = new ByteArrayInputStream(new byte[4096]);
			ReadChannel readChannel = Mockito.mock(ReadChannel.class);
			Mockito.when(readChannel.read(Mockito.any(ByteBuffer.class))).thenAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					return new Integer(-1);
				}
			});
			Mockito.when(mockedBlob.reader()).thenReturn(readChannel);
			Mockito.when(storage.get(Mockito.eq(validBlob))).thenReturn(mockedBlob);

			return storage;
		}
	}
}
