# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* /만 요청했을때, "index.html"로 가게하는 것도 서버에서 처리해줘야 한다. 전에는 당연해서 원래 되는 기능인줄 알았다.   

### 요구사항 2 - get 방식으로 회원가입
* get 방식으로 회원가입을 하면, 개인정보가 url에 queryString으로 전달되어 일반인에게도 노출된다. 그래서 이런 경우엔 post method를 사용해야한다.  

### 요구사항 3 - post 방식으로 회원가입
* HashMap의 생성은 new HashMap();
* BufferedReader br을 추출한 메소드의 인자로 넘기니 제대로 작동하지 않았다. 왜일까?

### 요구사항 4 - redirect 방식으로 이동
* 302 임시 redirection
* 서버가 header에 Location 이라는 정보를 주고 302코드를 클라이언트에 전달하면, 클라이언트는 302를 보고 redirection이라는 것을 알고, Location에 있는 url로 다시 요청을 보낸다. 
* 그래서 302는 결국 두 번의 요청을 보내게 된다.  

### 요구사항 5 - cookie
* log in여부는 cookie를 활용해 알려줄 수 있다. 
* cookie는 서버에서 생성해 client에게 준다. (클라이언트에서 자바스크립트에서 생성해 서버로 줄 수도 있다.)
* StringBuilder는 String을 만들어주는 class이고, String클래스에서 지원하지 않는 append, delete 메소드를 지원한다. copyOfArray 혹은 concat 보다 간편하게 String을 추가할 수 있다. 
* cookie는 생성 시 유효기간을 따로 정해주지 않았더니, 브라우저를 종료할 때까지 계속 따라다녔다.(크롬 기준)
 
### 요구사항 6 - stylesheet 적용
* content Type에 html이나 css처럼 정확히 표기하지 않고 all같은 것으로 표시한다면, 브라우저는 어떤 형식을 읽어야하는 지 몰라서 자원을 렌더링 하는데 시간이 더 오래걸릴 것이다.  

### heroku 서버에 배포 후
* 