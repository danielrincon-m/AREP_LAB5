# Docker y AWS

[![danielrincon-m](https://circleci.com/gh/danielrincon-m/AREP_LAB4.svg?style=svg)](https://app.circleci.com/pipelines/github/danielrincon-m/AREP_LAB4)
<!-- [![Heroku](img/heroku_long.png)](https://nanospring.herokuapp.com/nspapp/register) -->

## Descripción 📦

En este laboratorio desarrollaremos un proyecto web simple, en en cual cada uno de los módulos que lo componen estarán corriendo en contenedores de docker independientes en una máquina virtual AWS, sin embargo, estos módulos se podrán comunicar entre ellos a través de la red local. Los componentes del sistema se enumeran a continuación:

- Motor de bases de datos Mongodb corriendo en el puerto local 27017.
- 3 Instancias del servicio LogService escuchando en los puertos locales 35001, 35002 y 35003 respectivamente.
- Una instancia del servicio RoundRobin escuchando en el puerto público 8080.


### Implementación

La implementación se divide en dos partes, desarrollo y creación de contenedores, y despliegue de los contenedores en AWS. Haremos un breve recorrido por ambas partes.

#### Desarrollo y creación de contenedores

Dos de las tres partes que componen el proyecto fueron desarrolladas por nosotros mismos, estas son [RoundRobin](/RoundRobin) y [LogService](/LogService), su código fuente puede ser encontrado en los enlaces de cada una de ellas, y su arquitectura en el [documento de diseño](Lab5_AREP.pdf).

Luego de crear con éxito las partes y probarlas localmente en los servidores SparkWeb de cada una de ellas, procedimos a encapsularlas en contenedores Docker como se muestra continuación.

Inicialmente, construimos los contenedores de manera local con los siguientes comandos:

![Build LogService](/img/build_logservice.png)
![Build RoundRobin](/img/build_roundrobin.png)

Luego de esto, creamos dos repositorios en dockerhub, uno para cada una de nuestras imágenes, y las subimos con los siguientes comandos:

Mapeamos los repositorios a nuestros contenedores locales

![Tag LogService](/img/tag_logservice.png)
![Tag RoundRobin](/img/tag_roundrobin.png)

Subimos los contenedores a los repositorios

![Push LogService](img/push_logservice.png)
![Push RoundRobin](img/push_roundrobin.png)

Ahora que tenemos los contenedores subidos en nuestro repositorio, vamos a crear un archivo docker compose para instalarlos de manera sencilla en otras máquinas, el código de este archivo luce así:

``` YML
version: '2'

services:
    round:
        image: danielrincon/roundrobin:latest
        container_name: roundrobin
        ports:
            - "8080:6000"
        depends_on:
            - "logservice1"
            - "logservice2"
            - "logservice3"
            - "db"
        
    logservice1:
        image: danielrincon/logservice:latest
        container_name: logservice35001
        ports:
            - "35001:6000"
        depends_on:
            - "db"

    logservice2:
        image: danielrincon/logservice:latest
        container_name: logservice35002
        ports:
            - "35002:6000"
        depends_on:
            - "db"

    logservice3:
        image: danielrincon/logservice:latest
        container_name: logservice35003
        ports:
            - "35003:6000"
        depends_on:
            - "db"

    db:
        image: mongo:3.6.1
        container_name: mongodb
        ports:
            - 27017:27017
        command: mongod

volumes:
    mongodb:
    mongodb_config:
```

Como podemos observar tenemos una instancia de nuestro contenedor RoundRobin, el cuál hará el rol de servidor público y de balanceador de carga, tres instancias de nuestro contenedor LogService, los cuales se encargarán de escribir los logs en la base de datos, y una instancia de MongoDB que es nuestra base de datos no relacional.

Gracias a que **docker-compose** crea una red interna con un servicio DNS, podemos conectarnos entre contenedores por medio de sus propios nombres, un ejemplo de conexión a la base de datos corriendo en el contenedor llamado *mongodb* es el siguiente:

![Conexión MongoDB](img/conn_mongodb.png)

---

#### Despliegue de contenedores en AWS

Gracias a que subimos nuestro repositorio en Github, fué muy sencillo clonarlo en nuestra máquina virtual en AWS y ejecutar el *docker-compose* para instalar todos los contenedores. Inicialmente, instalamos el *docker-compose* con los siguiente comando:

![Install docker-compose](/img/compose-inst.png)
![Execute docker-compose](img/compose-exec.png)

Luego de esto, clonamos nuestro repositorio de Github, nos cambiamos a la carpeta e instalamos nuestros contenedores por medio de *docker-compose* de la siguiente manera:

![Deploy docker-compose](img/compose-deploy.png)

Una vez los contenedores se encuentran desplegados y en ejecución, podemos observar el estado de la red interna por medio del siguiente comando, y arrojándonos el siguiente resultado:

``` bash
docker network inspect <Network name>
```
![Local Network](img/network.png)

Acá nos pudimos dar cuenta de que todos los contenedores se encuentran dentro de la misma red y se pueden comunicar entre ellos. Por último, debemos abrir el puerto público de la máquina virtual, en nuestro caso el 8080, para que de esta forma sea accesible por cualquier persona.

![Open Port](img/open_port.png)

#### Resultado

Vamos a observar que sucede al agregar un nuevo registro:

![Register Before Add](img/reg_before.png)
![Register After Add](img/reg_after.png)

Como pudimos observar, se adicionó y se retornó correctamente el registro junto con los que se habían registrado anteriormente.

### Descarga del proyecto

Clone el proyecto utilizando el siguiente comando:

```
git clone https://github.com/danielrincon-m/AREP_LAB5.git
```

## Correr las pruebas unitarias 🧪

### Prerrequisitos

Un IDE que soporte proyectos Java, o una instalación de Maven en su sistema, puede obtenerlo desde
la [página oficial.][mvnLink]

### Ejecución de pruebas

Las pruebas pueden ser ejecutadas desde la sección de pruebas de su IDE o si tiene maven puede navegar a la carpeta
principal de cada uno de los dos proyectos internos y ejecutar el comando

```
mvn test
```

## Documentación del código fuente 🌎

La documentación de los proyectos puede ser encontrada en las carpetas [LogService/docs](LogService/docs) y [RoundRobin/docs](RoundRobin/docs).

También puede ser generada con Maven, clonando el proyecto y ejecutando el siguiente comando:

```
mvn javadoc:javadoc
```

## Documento de diseño 📄

El documento de diseño del programa puede ser encontrado [aquí](Lab5_AREP.pdf).

## Herramientas utilizadas 🛠️

* [Visual Studio Code](https://code.visualstudio.com/) - IDE de desarrollo
* [Maven](https://maven.apache.org/) - Manejo de Dependencias
* [JUnit](https://junit.org/junit4/) - Pruebas unitarias
* [GitHub](https://github.com/) - Repositorio de código
* [Docker](https://www.docker.com/) - Herramienta de encapsulamiento en contenedores
* [MongoDB](https://www.mongodb.com/es) - Base de datos
* [AWS](https://aws.amazon.com/es/) - Despliegue en la nube

## Autor 🧔

**Daniel Felipe Rincón Muñoz:** *Planeación y desarrollo del proyecto* -
[Perfil de GitHub](https://github.com/danielrincon-m)

## Licencia 🚀

Este proyecto se encuentra licenciado bajo **GNU General Public License** - consulte el archivo [LICENSE.md](LICENSE.md)
para más detalles.

<!-- 
## Acknowledgments 

* Hat tip to anyone whose code was used
* Inspiration
* etc
-->

[gitLink]: https://git-scm.com/downloads
[mvnLink]: https://maven.apache.org/download.cgi
