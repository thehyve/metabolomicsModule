package org.dbxp.metabolomicsModule.measurements

import org.dbxp.metabolomicsModule.identity.Feature

class MeasurementPlatformVersionFeature {
	
	MeasurementPlatformVersion measurementPlatformVersion
	Feature feature
	HashMap props

    static constraints = {
		props(nullable: true)
    }
	
	/*
	* returns the object as a HashMap to be used in the API
	*/
   HashMap toApi() {
	   return ['measurement_platform_version': this.measurementPlatformVersion.toApi(), 'feature': this.feature.toApi(), 'properties': this.props ?: [:]]
   }
	
}
