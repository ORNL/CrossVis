rm -rf deploy
mkdir deploy

$JDK_HOME/bin/javapackager \
	-deploy \
	-native installer \
	-name FalconFX \
	-title FalconFX \
	-vendor ORNL \
	-outdir ./deploy \
	-srcfiles ./coalesce-0.1.1-jar-with-dependencies.jar \
	-appclass gov.ornl.csed.cda.Falcon.FalconFX \
	-outfile FalconFX \
	-v