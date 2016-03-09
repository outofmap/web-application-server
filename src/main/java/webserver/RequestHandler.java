package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}",
				connection.getInetAddress(), connection.getPort());
		
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String firstline = br.readLine();
			String url = lineParser(firstline)[1];
			String request = lineParser(firstline)[0];
			Map<String,String> headerInfo = null;
			System.out.println("Request: " + request);
			System.out.println("URL: " + url);
			String param;
			// 유저 등록 코드
			if (request.equalsIgnoreCase("GET")) {
				int index = url.indexOf("?");
				if (url.equals("/")) {
					url = "/index.html";
				}
				if (index != -1) {
					String requestPath = url.substring(0, index);
					String prams = url.substring(index + 1);
					HttpRequestUtils requestUtil = new HttpRequestUtils();
					Map<String, String> parsedParms = HttpRequestUtils.parseQueryString(prams);
					User user = new User(parsedParms.get("userId"), parsedParms.get("password"),
							parsedParms.get("name"), parsedParms.get("email"));
					System.out.println(user.toString());
				}
			}
			headerInfo = checkHeader(br);
			if(request.equalsIgnoreCase("POST")){
				
			}
			
			//param이 공백일까? pras일까? 확인해야함  
			param = br.readLine();
			//ioUtils의 readData() 메서드에 body의 시작 br과 contentlength를 인자로 넣으면 body string return됨.
			IOUtils ioUtils = new IOUtils();
			//post일 때, body에 전달된 데이터 
			System.out.println("param:" + param);
			System.out.println("content-Length"+headerInfo.get("Content-Length"));
			
			DataOutputStream dos = new DataOutputStream(out);
			byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
			response200Header(dos, body.length);
			responseBody(dos, body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private Map<String,String> checkHeader(BufferedReader br) throws IOException {
		String line = br.readLine();
		Map<String,String> headerInfo = null;
		if (line == null) {
			return null;
		}
		while (!"".equals(line)) {
			System.out.println(line);
			line = br.readLine();
			String[] subHeader = line.split(": ");
			String title = subHeader[0];
			String headerData = subHeader[1];
			headerInfo.put(title, headerData);
		}
		return headerInfo;
	}

	private String[] lineParser(String line) {
		String[] tokens = line.split(" ");
		return tokens;
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
		//localhost써야할까?
		String url = "/index.html";
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: "+url+ "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
