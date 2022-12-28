package uit.app.document_scanner;

import java.util.List;

public class InputParam {
    private String name;
    private List<TextResult> list;

    public InputParam(String name, List<TextResult> list){
        this.name = name;
        this.list = list;
    }

    public List<TextResult> getTextResultList() {
        return list;
    }

    public String getKeyName() {
        return name;
    }
}
