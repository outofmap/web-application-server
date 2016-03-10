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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
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
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
				connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String requestLine = br.readLine();
			String url = lineParser(requestLine)[1];
			String requestMethod = lineParser(requestLine)[0];
			Map<String, String> headerInfo = new HashMap();
			System.out.println("Request: " + requestMethod);
			System.out.println("URL: " + url);
			String param;
			StringBuilder sb = new StringBuilder();

			// headerInfo 담기.
			String line = br.readLine();
			if (line == null) {
				return;
			}
			while (!"".equals(line)) {
				System.out.println(line);
				String[] subHeader = line.split(": ");
				String title = subHeader[0];
				String headerData = subHeader[1];
				log.debug(title);
				log.debug(headerData);
				headerInfo.put(title, headerData);
				line = br.readLine();
			}
			// GET요청 처리
			if (requestMethod.equalsIgnoreCase("GET")) {
				int index = url.indexOf("?");
				if (url.equals("/")) {
					log.debug("index");
					url = "/index.html";
					DataOutputStream dos = new DataOutputStream(out);
					byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
					response200Header(dos, body.length);
					responseBody(dos, body);
				}else if(url.equals("/user/list") ){
					if(headerInfo.get("Cookie").equals("logined=true")){
					Collection<User> users = DataBase.findAll();
					url = "/user/list.html";
					log.debug("user/list and Logined=ture");
					sb.append("<table>");
					sb.append("<tr>");
				//	while(users.iterator().)
					
//					<tr>
//                    <th>#</th> <th>사용자 아이디</th> <th>이름</th> <th>이메일</th><th></th>
//                </tr>
					
					DataOutputStream dos = new DataOutputStream(out);
					byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
					response200Header(dos, body.length);
					responseBody(dos, body);
					} else if(headerInfo.get("Cookie").equals("logined=false")){
						url = "/user/list.html";
						String redirectionURL = "/user/login.html";
						log.debug("logined=false");
						DataOutputStream dos = new DataOutputStream(out);
						byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
						response302Header(dos, body.length, redirectionURL);
						responseBody(dos, body);
					}
					
				} else{
					DataOutputStream dos = new DataOutputStream(out);
					byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
					response200Header(dos, body.length);
					responseBody(dos, body);
					
				}
				//GETmethod에 queryString이 있을 때
				if (index != -1) {
					String requestPath = url.substring(0, index);
					String queryString = url.substring(index + 1);
					HttpRequestUtils requestUtil = new HttpRequestUtils();
					Map<String, String> parsedParms = HttpRequestUtils.parseQueryString(queryString);
					User user = new User(parsedParms.get("userId"), parsedParms.get("password"),
							parsedParms.get("name"), parsedParms.get("email"));
				}
			}

			if (requestMethod.equalsIgnoreCase("POST")) {
				IOUtils ioUtils = new IOUtils();
				int contentLength = Integer.parseInt(headerInfo.get("Content-Length"));
				param = ioUtils.readData(br, contentLength);
				Map<String, String> parsedParms = HttpRequestUtils.parseQueryString(param);
				if (url.equals("/user/create")) {

					User user = new User(parsedParms.get("userId"), parsedParms.get("password"),
							parsedParms.get("name"), parsedParms.get("email"));
					System.out.println(user.toString());
					DataBase.addUser(user);
					String redirectUrl = "../../index.html";
					DataOutputStream dos = new DataOutputStream(out);
					byte[] body = Files.readAllBytes(new File("./webapp" + "/index.html").toPath());
					response302Header(dos, body.length, redirectUrl);
					responseBody(dos, body);
				} else if (url.equals("/user/login")) {
					String userId = parsedParms.get("userId");
					String userPassword = parsedParms.get("password");
					if (DataBase.findUserById(userId) != null) {
						User dbResultUser = DataBase.findUserById(userId);
						if(dbResultUser.getPassword().equals(userPassword)){
							DataOutputStream dos = new DataOutputStream(out);
							byte[] body = Files.readAllBytes(new File("./webapp" + "/index.html").toPath());
							responseLoginSuccess302Header(dos, body.length);
							responseBody(dos, body);
						}else {
							DataOutputStream dos = new DataOutputStream(out);
							byte[] body = Files.readAllBytes(new File("./webapp" + "/index.html").toPath());
							responseLoginFailed302Header(dos, body.length);
							responseBody(dos, body);
							
						}
					} else {
						//login failed
						DataOutputStream dos = new DataOutputStream(out);
						byte[] body = Files.readAllBytes(new File("./webapp" + "/index.html").toPath());
						responseLoginFailed302Header(dos, body.length);
						responseBody(dos, body);
						
					}

				}
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		}
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
	private void responseLoginFailed200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("Set-Cookie: logined=false \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	

	private void response302Header(DataOutputStream dos, int lengthOfBodyContent, String redirectionURL) {
		// localhost써야할까?
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: " +redirectionURL + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	private void responseLoginSuccess302Header(DataOutputStream dos, int lengthOfBodyContent) {
		// localhost써야할까?
		try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Set-Cookie: logined=true \r\n");
			dos.writeBytes("Location: " + "../../index.html" + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	private void responseLoginFailed302Header(DataOutputStream dos, int lengthOfBodyContent) {
		// localhost써야할까?
		try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Set-Cookie: logined=false \r\n");
			dos.writeBytes("Location: " + "./login_failed.html" + "\r\n");
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
