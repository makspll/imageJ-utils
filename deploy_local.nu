mvn package

let jar_name = ls ./target | find .jar | get name | first | str replace "target/" ""

# trim /target


mkdir ./ImageJ/plugins/makspll
mkdir ./Fiji/Fiji.app/plugins/makspll


cp $"./target/($jar_name)" $"./ImageJ/plugins/makspll/($jar_name)"
cp $"./target/($jar_name)" $"./Fiji/Fiji.app/plugins/makspll/($jar_name)"

# mkdir -p imageJ/plugins/makspll
# JAR_NAME=$(find ./target -name "*.jar")
# echo ${JAR_NAME}
# cp ${JAR_NAME} imageJ/plugins/makspll/${JAR_NAME}