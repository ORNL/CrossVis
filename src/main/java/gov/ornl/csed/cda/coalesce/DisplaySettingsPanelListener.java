package gov.ornl.csed.cda.coalesce;

import java.awt.*;

public interface DisplaySettingsPanelListener {
    public void selectedDataColorChanged(Color color);

    public void unselectedDataColorChanged(Color color);

    public void secondaryFontSizeChanged(int fontSize);

    public void titleFontSizeChanged(int fontSize);

    public void axisWidthChanged(int width);

    public void scatterplotSizeChanged(int size);

    public void correlationBoxSizeChanged(int size);

    public void pcLineSizeChanged(int size);
}
