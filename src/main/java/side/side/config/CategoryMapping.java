//test jamannana
package side.side.config;

import java.util.HashMap;
import java.util.Map;

public class CategoryMapping {
    private static final Map<String, String[]> categoryMap = new HashMap<>();

    static {
        categoryMap.put("관광지", new String[]{
                "A01010100", "A01010200", "A01010300", "A01010400", "A01010500",
                "A01010600", "A01010700", "A01010800", "A01010900", "A01011000",
                "A01011100", "A01011200", "A01011300", "A01011400", "A01011600",
                "A01011700", "A01011800", "A01011900", "A01020100", "A01020200",
                "A02010100", "A02010200", "A02010300", "A02010400", "A02010500",
                "A02010600", "A02010700", "A02010800", "A02010900", "A02011000",
                "A02020200", "A02020300", "A02020400", "A02020500", "A02020600",
                "A02020700", "A02020800", "A02030100", "A02030200", "A02030300",
                "A02030400", "A02030600", "A02040400", "A02040600", "A02040800",
                "A02040900", "A02041000", "A02050100", "A02050200", "A02050300",
                "A02050400", "A02050500", "A02050600"
        });
        categoryMap.put("문화시설", new String[]{
                "A02060100", "A02060200", "A02060300", "A02060400", "A02060500",
                "A02060600", "A02060700", "A02060800", "A02060900", "A02061000",
                "A02061100", "A02061200", "A02061300", "A02061400"
        });
        categoryMap.put("행사", new String[]{
                "A02070100", "A02070200", "A02080100", "A02080200", "A02080300",
                "A02080400", "A02080500", "A02080600", "A02080800", "A02080900",
                "A02081000", "A02081100", "A02081200", "A02081300"
        });
        categoryMap.put("여행코스", new String[]{
                "C01120001", "C01130001", "C01140001", "C01150001", "C01160001", "C01170001"
        });
        categoryMap.put("레포츠", new String[]{
                "A03010200", "A03010300", "A03020200", "A03020300", "A03020400",
                "A03020500", "A03020600", "A03020700", "A03020800", "A03020900",
                "A03021000", "A03021100", "A03021200", "A03021300", "A03021400",
                "A03021500", "A03021600", "A03021700", "A03021800", "A03022000",
                "A03022100", "A03022200", "A03022300", "A03022400", "A03022600",
                "A03022700", "A03030100", "A03030200", "A03030300", "A03030400",
                "A03030500", "A03030600", "A03030700", "A03030800", "A03040100",
                "A03040200", "A03040300", "A03040400", "A03050100"
        });
        categoryMap.put("숙박", new String[]{
                "B02010100", "B02010500", "B02010600", "B02010700", "B02010900",
                "B02011000", "B02011100", "B02011200", "B02011300", "B02011600"
        });
        categoryMap.put("쇼핑", new String[]{
                "A04010100", "A04010200", "A04010300", "A04010400", "A04010500",
                "A04010600", "A04010700", "A04010900", "A04011000"
        });
        categoryMap.put("음식", new String[]{
                "A05020100", "A05020200", "A05020300", "A05020400", "A05020700",
                "A05020900", "A05021000"
        });
    }

    public static String[] getCategoryCodes(String categoryName) {
        return categoryMap.get(categoryName);
    }
}
