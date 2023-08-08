# APC
![image](https://github.com/jusenglee/APC/assets/85321903/3f01a069-cca7-4ebf-abce-a535002e0f1c)

![image](https://github.com/jusenglee/APC/assets/85321903/505640b7-a4b9-4a8f-a72b-b8e12456b968)

사용한 스택:
mybatis
Pebble
Java-Spring

관리자 화면에서 사용자에게 입력받은 값을 서버 - DB로 전송한다.
뷰 - DTO - 컨트롤러 - VO - DB
방식으로 값을 insert 한다.

본 작업을 할때, DB에 테이블 구조가 유형코드 필드 하나만으로 각 데이터를 구분하기에 뷰와 서버단 중 어느곳에서 데이터를 구분해야할지 고민하다
DTO를 구현하여 뷰에서 각 데이터를 구분하고, 서버는 구분되어 온 데이터를 통합하는 방식을 채용했다.

기존에는 JSP(JSTL), 타임리프만을 사용하다 Pebble을 이때 처음 써봤는데, 뷰 화면 구현에 있어서  
추가버튼을 Pebble이 for 루프를 통해 여러개를 구현하는 부분이 난제였는데, Pebble의 loop.index를 hidden div로 만들어서 값을 저장하고, JS에서 접근하는 방식으로 구현했다.
데이터를 불러올때는 DTO 내부에 private List<ApcVO> apcDataList = new ArrayList<>(); 를 추가하여 ArrayList로 각 객체를 리스트 형식으로 불러오게 된다.
입력받는 칸도 많고, 버튼에 따른 화면제어가 많다보니 자연스럽게 스크립트가 길어졌는데 이 부분은 차후 최적화 예정.
