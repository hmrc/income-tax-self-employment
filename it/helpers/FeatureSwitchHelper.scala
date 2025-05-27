
package helpers

object FeatureSwitchHelper {

  def enable(featureSwitch: String): Unit = System.setProperty(featureSwitch, "true")

  def disable(featureSwitch: String): Unit = System.setProperty(featureSwitch, "false")

}
