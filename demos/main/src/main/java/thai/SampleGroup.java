package thai;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王心觉 on 2018/8/24.
 */

class SampleGroup {

    public final String title;
    public final List<Sample> samples;

    public SampleGroup(String title) {
        this.title = title;
        this.samples = new ArrayList<>();
    }

}
