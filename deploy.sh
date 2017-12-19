#!/bin/bash
# Salir del script si alguna de las ejecuciones falla.
set -e

# ======================================================================================================================================
# Parametros por defecto
# ======================================================================================================================================

# Parámetro que indica el directorio en que se va a trabajar.
WORKING_DIRECTORY=/tmp/CNVR
# Número de terminales por defecto.
DEFAULT_SIZE=3
# Activar o desactivar modo debug (true o false).
DEFAULT_DEBUG=false

# ======================================================================================================================================
# Funciones
# ======================================================================================================================================

# Funcion para imprimir '=' hasta el final de la linea.
line () {
	for i in $(seq 1 $(stty size | cut -d' ' -f2)); do 
		echo -n "="
	done
	echo ""
}

# Imprime ayuda por pantalla.
print_help () {
	echo "Parámetros:"
	echo ""
	echo "   --size=n:  levanta n procesos. Por defecto: 3."
	echo "   --debug:   activa o desactiva el modo debug. Por defecto: desactivado."
	echo ""
	echo "Ejemplos:"
	echo ""
	echo "   ./zookeeper.sh"
	echo "   ./zookeeper.sh --size=4"
	echo "   ./zookeeper.sh --size=4 --debug"
}

# ======================================================================================================================================
# Lectura de parametros configurables
# ======================================================================================================================================

# Parametros pasados por consola con nombre concreto
while [ $# -gt 0 ]; do
	case "$1" in
		--size=*)
			SIZE="${1#*=}"
			;;
		--debug)
			DEBUG=true
			;;
		*)
			print_help
			exit 1
	esac
	shift
done

# Valores por defecto para las variables.
if [ ${#SIZE} -lt 1 ]; then
	SIZE=$DEFAULT_SIZE
fi
if [ ${#DEBUG} -lt 1 ]; then
	DEBUG=$DEFAULT_DEBUG
fi

# Variables que almacenan el contenido de las directivas a pasarle al Java.
if $DEBUG; then
	DEBUG_DIRECTIVE="--debug"
	echo "Modo debug activado"
fi
SIZE_DIRECTIVE="--size=$SIZE"

# Mensaje informativo sobre el número de servidores que se levantaran.
echo "El numero de servidores que se levantaran es: $SIZE"

# ======================================================================================================================================
# Main
# ======================================================================================================================================

# Crear directorio de trabajo, y directorio de datos de Zookeeper para cada host.
mkdir -p $WORKING_DIRECTORY
mkdir -p $WORKING_DIRECTORY/z1
mkdir -p $WORKING_DIRECTORY/z2
mkdir -p $WORKING_DIRECTORY/z3

# Borrar bases de datos previas si existen.
if [ -d $WORKING_DIRECTORY/dbs ]; then
	rm -r $WORKING_DIRECTORY/dbs
fi
mkdir $WORKING_DIRECTORY/dbs

# Si existe el repo, hacer pull; si no, clonar.
if [ -d "$WORKING_DIRECTORY/zookeeper" ]; then
	cd $WORKING_DIRECTORY/zookeeper && git pull
else
	cd $WORKING_DIRECTORY && git clone https://github.com/revilla-92/zookeeper.git
fi

# Mover al directorio de trabajo.
cd $WORKING_DIRECTORY

# Extraer tar.gz en el directorio zookeeper.
cp ./zookeeper/zk/zookeeper-3.4.10.tar.gz .
tar -zxvf zookeeper-3.4.10.tar.gz
rm zookeeper-3.4.10.tar.gz

# Copiar librerías al directorio de trabajo.
cp -r ./zookeeper/lib .

# Copiar JAR al directorio de trabajo.
cp ./zookeeper/pfinal.jar .

# Copiar configuración al directorio de configuración de Zookeeper.
cp -r ./zookeeper/conf/* ./zookeeper-3.4.10/conf

# Modificar directorio de datos en archivos de configuración mediante sed.
find . -wholename ./zookeeper-3.4.10/conf/localhost_zoo1.cfg -type f -exec sed -i s#"WORKING_DIRECTORY"#"$WORKING_DIRECTORY"#g {} +
find . -wholename ./zookeeper-3.4.10/conf/localhost_zoo2.cfg -type f -exec sed -i s#"WORKING_DIRECTORY"#"$WORKING_DIRECTORY"#g {} +
find . -wholename ./zookeeper-3.4.10/conf/localhost_zoo3.cfg -type f -exec sed -i s#"WORKING_DIRECTORY"#"$WORKING_DIRECTORY"#g {} +

# Crear archivos de descripción de los hosts en el directorio de datos.
echo 1 > ./z1/myid
echo 2 > ./z2/myid
echo 3 > ./z3/myid

# Cambiar al directorio de zookeeper.
cd ./zookeeper-3.4.10

# Conceder permisos de ejecución a los binarios de Zookeeper, y de escritura al usuario root.
chmod 755 ./bin/*.sh

# Exportar classpath.
export CLASSPATH=$CLASSPATH:$WORKING_DIRECTORY/pfinal.jar:$CLASSPATH:$WORKING_DIRECTORY/lib/*

# ======================================================================================================================================
# Levantar sistema distribuido
# ======================================================================================================================================

# Si nos encontramos en los ordenadores del laboratorio necesitamos realizar esta acción antes.
line
echo "Si se ejecuta desde un PC del laboratorio ejecutar antes:"
echo " ln -sf /opt/Oracle/jdk1.8 java_home"

# Arrancar los servidores que conformarán el entorno zookeeper.
line
echo "Arrancando el servidor 1"
$WORKING_DIRECTORY/zookeeper-3.4.10/bin/zkServer.sh start $WORKING_DIRECTORY/zookeeper-3.4.10/conf/localhost_zoo1.cfg
line
echo "Arrancando el servidor 2"
$WORKING_DIRECTORY/zookeeper-3.4.10/bin/zkServer.sh start $WORKING_DIRECTORY/zookeeper-3.4.10/conf/localhost_zoo2.cfg
line
echo "Arrancando el servidor 3"
$WORKING_DIRECTORY/zookeeper-3.4.10/bin/zkServer.sh start $WORKING_DIRECTORY/zookeeper-3.4.10/conf/localhost_zoo3.cfg

# Verificamos el estado de los servidores del entorno Zookeeper.
line
echo "El estado del servidor 1 de Zookeeper:"
$WORKING_DIRECTORY/zookeeper-3.4.10/bin/zkServer.sh status $WORKING_DIRECTORY/zookeeper-3.4.10/conf/localhost_zoo1.cfg
line
echo "El estado del servidor 2 de Zookeeper:"
$WORKING_DIRECTORY/zookeeper-3.4.10/bin/zkServer.sh status $WORKING_DIRECTORY/zookeeper-3.4.10/conf/localhost_zoo2.cfg
line
echo "El estado del servidor 3 de Zookeeper:"
$WORKING_DIRECTORY/zookeeper-3.4.10/bin/zkServer.sh status $WORKING_DIRECTORY/zookeeper-3.4.10/conf/localhost_zoo3.cfg

# Lanzar mensajes en consola con instrucciones de ejecución en varias terminales.
line
echo "Ejecutar los siguientes comandos para acceder a la CLI (Command Line Interface) de cada uno de los servidores del conjunto Zookeeper:"
echo " $WORKING_DIRECTORY/zookeeper-3.4.10/bin/zkCli.sh -server localhost:2181"
echo " $WORKING_DIRECTORY/zookeeper-3.4.10/bin/zkCli.sh -server localhost:2182"
echo " $WORKING_DIRECTORY/zookeeper-3.4.10/bin/zkCli.sh -server localhost:2183"
line

# ======================================================================================================================================
# Levantar procesos
# ======================================================================================================================================

# Cambiamos al directorio donde se encuentra el programa y lo ejecutamos.
cd $WORKING_DIRECTORY

# Levantar tantas treminales como indique $SIZE.
for((n=0;n<$SIZE;n++));
do
	xterm -hold -e "export CLASSPATH=$CLASSPATH:$WORKING_DIRECTORY/pfinal.jar:$CLASSPATH:$WORKING_DIRECTORY/lib/* && java -Djava.net.preferIPv4Stack=true es.upm.dit.cnvr.pfinal.MainBank $DEBUG_DIRECTIVE --size=$SIZE" &
done
