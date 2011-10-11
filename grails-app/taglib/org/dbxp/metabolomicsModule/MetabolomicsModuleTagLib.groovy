package org.dbxp.metabolomicsModule

import org.dbxp.dbxpModuleStorage.UploadedFile
import org.dbxp.metabolomicsModule.measurements.MeasurementPlatformVersion

/**
 * The metabolomics module tag library delivers a rich set of tags to make it easier to re-use components
 * and elements in the interface.
 *
 * @version: $Rev$
 * @author: $Author$
 * @lastrevision: $Date$
*/

class MetabolomicsModuleTagLib {

    // abbreviation for Metabolomics Module
    static namespace = "mm"

    def assayService
    def uploadedFileService
    def measurementService

    def uploadedFileList = { attrs ->

		attrs.dialogProperties['baseUrl'] = resource('/', absolute: true);

		// get file list from attributes
  		def uploadedFiles
		if (attrs.containsKey('files')) {
			uploadedFiles = attrs.get('files')
		} else {
			throw new Exception("required attribute 'files' is missing from the <uploadedFilesList files=\"...\"/> tag")
		}

		// output file uploadr
        out << '<h1>Uploaded files</h1>'
        out << uploadr.add(name: "uploadrArea", path: "/tmp", placeholder: "Drop file(s) here to upload", direction: 'up', maxVisible: 8, rating: true, colorPicker: true, voting: true) {
            uploadedFiles.each { uploadedFile ->
				// add file to the uploadr
                uploadr.file(name: uploadedFile.fileName) {
					uploadr.deletable { ((uploadedFile.uploader.id == session.user.id || session.user.isAdministrator)) }
					if (uploadedFile.hasProperty('color') && uploadedFile.color) uploadr.color { "${uploadedFile.color}" }
					uploadr.rating { "${uploadedFile.getRating()}" }
					uploadr.fileSize { uploadedFile.fileSize }
                    uploadr.fileModified { uploadedFile.lastUpdated.time }
                    uploadr.fileId { uploadedFile.id }
                }
            }

            uploadr.onSuccess {
				out << g.render(template:'/js/uploadr/onSuccess', model:[])
            }

            uploadr.onFailure {
                "console.log('failed uploading ' + file.fileName);"
            }

            uploadr.onAbort {
                "console.log('aborted uploading ' + file.fileName);"
            }

			uploadr.onView {
				out << g.render(template:'/js/uploadr/onView',
					model:[dialogProperties: attrs.dialogProperties]);
            }

            uploadr.onDownload {
				out << g.render(template:'/js/uploadr/onDownload', model:[])
            }

            uploadr.onDelete {
				out << g.render(template:'/js/uploadr/onDelete', model:[])
            }

			uploadr.onChangeColor {
				out << g.render(template:'/js/uploadr/onChangeColor', model:[])
			}
        }
    }

    def studyList = { attrs ->

        out << '<h1>Study overview</h1>'

        // find all studies the user can read and have at least one assay
        def readableStudiesWithAssays = assayService.getAssaysReadableByUserAndGroupedByStudy(session.user)
        out << '<ul class=studyList>'

        readableStudiesWithAssays.each { study, assays ->
            out << studyTag(study: study, assays: assays)
        }

        out << '</ul>'

    }
    
    def studyTag = { attrs ->

        out << '<li class="studyTag">' + attrs.study.name + '<span class="sampleCount">' + attrs.assays.collect{it.samples?.size()}.sum() + ' samples</span><ul class="assayList">'

        attrs.assays.each { assay ->
            out << assayTag(assay: assay)
        }

        out << '</ul></li>'

    }

    def assayTag = { attrs ->

        //TODO: present the information in a better/nicer way

        def assay = attrs.assay

        def sampleMsg = "${assay.samples?.size()} samples"

        Long uploadedFileId = UploadedFile.findByAssay(assay)?.id
        UploadedFile uploadedFile = UploadedFile.get(uploadedFileId)

        if (uploadedFile) {
            if (uploadedFile.matrix) sampleMsg += " (${uploadedFile.determineAmountOfSamplesWithData()} assigned)";

            if (uploadedFile['platformVersionId']) {
                def mpv = MeasurementPlatformVersion.get((Long) uploadedFile['platformVersionId'])
				if (mpv){
					sampleMsg += " ${mpv.measurementPlatform?.name} ($mpv.versionNumber)"
				}
            }
        }

        out << "<li class=\"assayTag\" >"
		out << 		g.link(action:"view", controller:"assay", id: assay.id) { assay.name }
        out << "	<span class=sampleCount>"
		out <<			sampleMsg
		out << "	</span>"
		out << "</li>"

    }

    /**
     * Dropdown list control to choose the platform used (DCL lipodomics, et cetera).
     *
     * @param assay
     */
    def assayPlatformChooser = { attrs, body ->
        out << "Platform:  <br />"
        out << '<form>'
        out << '<select name="platformVersionId" size="8" style="width:100%;" ' + (attrs.disabled ? 'disabled>' : '>')

        measurementService.findAllMeasurementPlatforms().each { platform ->
			// if new studygroup create new label
			out << '<optgroup label="' + platform.name + '">'
            measurementService.findAllMeasurementPlatformVersions(measurementPlatform:platform).each { platformVersion ->

                out << '<option value="' + platformVersion.id + '" ' + ((platformVersion.id == attrs.assay.measurementPlatformVersion.id) ? 'selected' : '') + '>' + platformVersion.versionNumber + '</option>'
            }

            out << '</optgroup>'

        }

        out << '</select>'
        out << '<input type="submit" value="Submit" />'
        out << '</form>'
    }

	def commentFieldEditor = { attrs, body ->

		out << "Comments: <br />"
		out << '<form>'
		out << '<textarea name="comments" rows="8" style="width:100%;padding:0;">'
		out << attrs.assay.comments
		out << '</textarea>'
		out << '<br /><input type="submit" value="Submit" />'
		out << '</form>'

	}

}
