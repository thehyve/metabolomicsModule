package org.dbxp.metabolomicsModule

import org.dbxp.dbxpModuleStorage.UploadedFile
import org.dbxp.metabolomicsModule.measurements.MeasurementPlatform
import org.dbxp.metabolomicsModule.measurements.MeasurementPlatformVersion

class AssayController {
	
	/*
	 * embed required service(s)
	 */
	def assayService

	
	/*
	 * Assay by Token (for GSCF integration)
	 */
	def showByToken = {
		if (params.id){
			def assay = MetabolomicsAssay.findByAssayToken(params.id as String)
			redirect (action: "view", id: assay.id, params: params)
		}
		
		response.sendError(400, "No assayToken specified.") 
	}
	
	/*
	 * Metabolomics Assay page
	 * - list basic assay info (name, members etc)
	 * - list files related to this assay
	 * 
	 * params.id is required to load the assay
	 */
	def view = {
		if (!params.id) response.sendError(400, "No assay id specified.") // id of an assay must be present
		
		// load assay from id (for session.user)
		MetabolomicsAssay assay = assayService.getAssayReadableByUserById(session.user, params.id as Long)

        if (!assay) {
            response.sendError(404, "No assay found with id $params.id")
            return
        } else if (params.platformVersionId) {
            assay.measurementPlatformVersion = MeasurementPlatformVersion.get(params.platformVersionId)
            assay.save()
        } else if (params.comments) {
			assay.comments = params.comments
			assay.save()
		}
		
		def assayFiles = UploadedFile.findAllByAssay(assay)

		def measurementPlatformVersions = []
		def measurementPlatformVersionUploadedFiles = [:]
		assayFiles.each{ assayFile ->
			
			// get MeasurementPlatformVersion from AssayFile
			def mpv = MeasurementPlatformVersion.get((Long) assayFile['platformVersionId'])

            println mpv
            if (mpv) {
                // add MeasurementPlatformVersion to List
                measurementPlatformVersions.add(mpv)

                // prepare a Map with MeasuremtentPlatformVersions and their linked files
                if (!measurementPlatformVersionUploadedFiles[mpv.id]) { measurementPlatformVersionUploadedFiles[mpv.id] = [] }
                measurementPlatformVersionUploadedFiles[mpv.id] << assayFile
            }
		}

        def assayFeatures = [:]
        def featureFeatures = [:]

        assay.measurementPlatformVersion?.features?.each { mpvf ->
            assayFeatures[mpvf.feature.label] = mpvf.props
            featureFeatures[mpvf.feature.label] = mpvf.feature.props
        }
				
		[	assay: assay,
			assayFiles: assayFiles,
			measurementPlatformVersions: measurementPlatformVersions,
			measurementPlatformVersionUploadedFiles: measurementPlatformVersionUploadedFiles,
            assayFeatures: assayFeatures,
            featureFeatures: featureFeatures
        ]
	}
}
