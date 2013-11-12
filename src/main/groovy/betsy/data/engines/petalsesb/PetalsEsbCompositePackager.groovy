package betsy.data.engines.petalsesb

import ant.tasks.AntUtil
import betsy.Configuration
import betsy.data.BetsyProcess

class PetalsEsbCompositePackager {

    final AntBuilder ant = AntUtil.builder()
    BetsyProcess process

    void build() {
        createBinding()
        createComposite()
    }

    private void createComposite() {
        // create composite
        String compositeDir = "${process.targetTmpPath}/composite"
        String compositeMetaDir = "$compositeDir/META-INF"
        ant.mkdir dir: compositeMetaDir
        ant.xslt(in: process.targetBpelFilePath, out: "$compositeMetaDir/jbi.xml", style: "${process.engine.xsltPath}/create_composite_jbi_from_bpel.xsl")
        ant.move file: process.targetPackageFilePath, todir: compositeDir
        ant.copy file: bindingArchive, todir: compositeDir

        ant.replace(dir: compositeDir, token: "PARTNER_IP_AND_PORT", value: Configuration.get("partner.ipAndPort"))
        ant.replace(dir: compositeMetaDir, token: "PARTNER_IP_AND_PORT", value: Configuration.get("partner.ipAndPort"))

        // build composite
        ant.zip file: process.targetPackageCompositeFilePath, basedir: compositeDir
    }

    void createBinding() {
        String bindingDir = "${process.targetTmpPath}/binding"
        String bindingMetaDir = "$bindingDir/META-INF"
        ant.mkdir dir: bindingDir
        ant.mkdir(dir: bindingMetaDir)
        ant.xslt(in: process.targetBpelFilePath, out: "$bindingMetaDir/jbi.xml", style: "${process.engine.xsltPath}/create_binding_jbi_from_bpel.xsl")
        ant.copy(todir: bindingDir) {
            fileset(dir: process.targetBpelPath, includes: "*.xsd")
            fileset(dir: process.targetBpelPath, includes: "*.wsdl")
        }

        ant.replace(dir: bindingDir, token: "PARTNER_IP_AND_PORT", value: Configuration.get("partner.ipAndPort"))
        ant.replace(dir: bindingMetaDir, token: "PARTNER_IP_AND_PORT", value: Configuration.get("partner.ipAndPort"))

        ant.zip(file: bindingArchive, basedir: bindingDir)
    }

    private GString getBindingArchive() {
        "${process.targetTmpPath}/${process.name}Binding.zip"
    }

}
