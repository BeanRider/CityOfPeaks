package mvc.view;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

import mvc.model.CityOfPeaks_DataModel;
import mvc.model.IEnergyVizModel;
import mvc.view.visual.BaseCompositeTransformableVisual;
import mvc.view.visual.BaseTransformableVisual;
import mvc.view.visual.FixedVisual;

/**
 * Encapsulates all graphical components of the 3D visualization; it is an composite 3D component:
 * - when mutated, must mutate all enclosed 3D components as well.
 */
public class CityOfPeaks3D extends BaseCompositeTransformableVisual {

  public CityOfPeaks3D(CityOfPeaks_DataModel cityOfPeaksDataModel) {

    Objects.requireNonNull(cityOfPeaksDataModel);

    super.visual3D = new ArrayList<>(3);
    // Base grid layer
    super.visual3D.add(new Grid3D.Grid3DBuilder().setCellSize(150).setLines(40, 40).build());
    // Building outline layer
    super.visual3D.add(new BuildingOutlineMap());
    // Building extrusions and flags layer
    super.visual3D.add(new PeakMap(cityOfPeaksDataModel));

    super.fixedVisuals = new ArrayList<>(0);

    super.setCornerXYZ(0, 0, 1);
  }

  @Override
  public void draw(CityOfPeaks_View parentView, IEnergyVizModel dataModel) {
    for (BaseTransformableVisual transformableChild : super.visual3D) {
      transformableChild.draw(parentView, dataModel);
    }

    for (FixedVisual fixedVisual : super.fixedVisuals) {
      fixedVisual.draw(parentView, dataModel);
    }
  }

  @Override
  public Dimension getDimension() {
    return new Dimension(0, 0);
  }
}
