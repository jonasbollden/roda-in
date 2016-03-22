package org.roda.rodain.schema.ui;

import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.utils.FontAwesomeImageCreator;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipPreviewNode extends TreeItem<String> implements Observer {
  private SipPreview sip;
  private Image iconBlack, iconWhite;

  private boolean blackIconSelected = true;

  /**
   * Creates a new SipPreviewNode
   *
   * @param sip
   *          The SipPreview object to be wrapped
   * @param iconBlack
   *          The icon to be used in the SipPreviewNode, with the color black
   * @param iconWhite
   *          The icon to be used in the SipPreviewNode, with the color white
   */
  public SipPreviewNode(SipPreview sip, Image iconBlack, Image iconWhite) {
    super(sip.getTitle());
    this.sip = sip;
    this.iconBlack = iconBlack;
    this.iconWhite = iconWhite;
    setGraphic(new ImageView(iconBlack));
  }

  /**
   * @return The SipPreview object that the SipPreviewNode is wrapping
   */
  public SipPreview getSip() {
    return sip;
  }

  /**
   * @return The SipPreviewNode's icon
   */
  public Image getIcon() {
    if (blackIconSelected) {
      return iconBlack;
    } else
      return iconWhite;
  }

  public Image getIconBlack() {
    return iconBlack;
  }

  public void setDescriptionLevel(String descLevel) {
    sip.setDescriptionlevel(descLevel);
    ResourceBundle hierarchyConfig = ResourceBundle.getBundle("properties/roda-description-levels-hierarchy");
    String category = hierarchyConfig.getString("category." + sip.getDescriptionlevel());
    String unicode = hierarchyConfig.getString("icon." + category);

    iconBlack = FontAwesomeImageCreator.generate(unicode);
    iconWhite = FontAwesomeImageCreator.generate(unicode, Color.WHITE);
    this.setGraphic(new ImageView(iconBlack));
  }

  public void setBlackIconSelected(boolean value) {
    blackIconSelected = value;
  }

  /**
   * @return True if the SipPreview's content has been modified, false otherwise
   * @see SipPreview#isContentModified()
   */
  public boolean isContentModified() {
    return sip.isContentModified();
  }

  /**
   * Forces the redraw of the item
   *
   * @param o
   * @param arg
   */
  @Override
  public void update(Observable o, Object arg) {
    Platform.runLater(() -> {
      String value = getValue();
      setValue("");
      setValue(value); // this forces a redraw of the item
    });
  }
}