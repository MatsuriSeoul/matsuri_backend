package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import side.side.model.GyeonggiEvent;
import side.side.model.SeoulEvent;
import side.side.repository.GyeonggiEventRepository;
import side.side.repository.SeoulEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private GyeonggiEventRepository gyeonggiEventRepository;

    @Autowired
    private SeoulEventRepository seoulEventRepository;

    private final String gyeonggiApiKey = "77b3011d245e4ca68e85caec7fd610ae";
    private final String seoulApiKey = "754578757270626739386969624e71";

    public void fetchAndSaveGyeonggiEvents() {
        int pageSize = 100;
        int startIndex = 1;
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
            String url = String.format("https://openapi.gg.go.kr/GGCULTUREVENTSTUS?KEY=%s&Type=json&pIndex=%d&pSize=%d",
                    gyeonggiApiKey, startIndex, pageSize);
            try {
                String response = restTemplate.getForObject(url, String.class);
                System.out.println("경기도 API 응답" + response);

                if (response.startsWith("<")) {
                    throw new IllegalArgumentException("존재하지 않는 응답 API");
                }

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode dataNode = rootNode.path("GGCULTUREVENTSTUS").path(1).path("row");

                if (dataNode.isArray()) {
                    List<GyeonggiEvent> events = new ArrayList<>();
                    for (JsonNode node : dataNode) {
                        GyeonggiEvent event = new GyeonggiEvent();
                        event.setInstNm(node.path("INST_NM").asText());
                        event.setTitle(node.path("TITLE").asText());
                        event.setCategoryNm(node.path("CATEGORY_NM").asText());
                        event.setUrl(node.path("URL").asText());
                        event.setImageUrl(node.path("IMAGE_URL").asText());
                        event.setBeginDe(node.path("BEGIN_DE").asText());
                        event.setEndDe(node.path("END_DE").asText());
                        event.setAddr(node.path("ADDR").asText(null));
                        event.setEventTmInfo(node.path("EVENT_TM_INFO").asText());
                        event.setPartcptExpnInfo(node.path("PARTCPT_EXPN_INFO").asText());
                        event.setTelnoInfo(node.path("TELNO_INFO").asText());
                        event.setHostInstNm(node.path("HOST_INST_NM").asText());
                        event.setHmpgUrl(node.path("HMPG_URL").asText());
                        event.setWritngDe(node.path("WRITNG_DE").asText());
                        events.add(event);
                    }
                    gyeonggiEventRepository.saveAll(events);

                    if (dataNode.size() < pageSize) {
                        moreData = false;
                    } else {
                        startIndex++;
                    }
                } else {
                    moreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                moreData = false;
            }
        }
    }

    public void fetchAndSaveSeoulEvents() {
        int pageSize = 100;
        int startIndex = 1;
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
            String url = String.format("http://openAPI.seoul.go.kr:8088/%s/json/ListPublicReservationCulture/%d/%d/",
                    seoulApiKey, startIndex, startIndex + pageSize - 1);
            try {
                String response = restTemplate.getForObject(url, String.class);
                System.out.println("서울 API 응답" + response);

                if (response.startsWith("<")) {
                    throw new IllegalArgumentException("JSON 응답 API 안됨");
                }

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode dataNode = rootNode.path("ListPublicReservationCulture").path("row");

                if (dataNode.isArray()) {
                    List<SeoulEvent> events = new ArrayList<>();
                    for (JsonNode node : dataNode) {
                        SeoulEvent event = new SeoulEvent();
                        event.setGubun(node.path("GUBUN").asText());
                        event.setSvcid(node.path("SVCID").asText());
                        event.setMaxclassnm(node.path("MAXCLASSNM").asText());
                        event.setMinclassnm(node.path("MINCLASSNM").asText());
                        event.setSvcstatnm(node.path("SVCSTATNM").asText());
                        event.setSvcnm(node.path("SVCNM").asText());
                        event.setPayatnm(node.path("PAYATNM").asText());
                        event.setPlacenm(node.path("PLACENM").asText());
                        event.setUsetgtinfo(node.path("USETGTINFO").asText());
                        event.setSvcurl(node.path("SVCURL").asText());
                        event.setX(node.path("X").asText());
                        event.setY(node.path("Y").asText());
                        event.setSvcopnbgndt(node.path("SVCOPNBGNDT").asText());
                        event.setSvcopnenddt(node.path("SVCOPNENDDT").asText());
                        event.setRcptbgndt(node.path("RCPTBGNDT").asText());
                        event.setRcptenddt(node.path("RCPTENDDT").asText());
                        event.setAreanm(node.path("AREANM").asText());
                        event.setImgurl(node.path("IMGURL").asText());
                        event.setDtlcont(node.path("DTLCONT").asText());
                        event.setTelno(node.path("TELNO").asText());
                        event.setVMin(node.path("V_MIN").asText());
                        event.setVMax(node.path("V_MAX").asText());
                        event.setRevstddaynm(node.path("REVSTDDAYNM").asText());
                        event.setRevstdday(node.path("REVSTDDAY").asText());
                        events.add(event);
                    }
                    seoulEventRepository.saveAll(events);

                    if (dataNode.size() < pageSize) {
                        moreData = false;
                    } else {
                        startIndex += pageSize;
                    }
                } else {
                    moreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                moreData = false;
            }
        }
    }
}
