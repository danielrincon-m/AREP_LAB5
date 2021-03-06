# Docker y AWS

[![danielrincon-m](https://circleci.com/gh/danielrincon-m/AREP_LAB4.svg?style=svg)](https://app.circleci.com/pipelines/github/danielrincon-m/AREP_LAB4)
<!-- [![Heroku](img/heroku_long.png)](https://nanospring.herokuapp.com/nspapp/register) -->

## Descripci贸n 馃摝

En este laboratorio desarrollaremos un proyecto web simple, en en cual cada uno de los m贸dulos que lo componen estar谩n corriendo en contenedores de docker independientes en una m谩quina virtual AWS, sin embargo, estos m贸dulos se podr谩n comunicar entre ellos a trav茅s de la red local. Los componentes del sistema se enumeran a continuaci贸n:

- Motor de bases de datos Mongodb corriendo en el puerto local 27017.
- 3 Instancias del servicio LogService escuchando en los puertos locales 35001, 35002 y 35003 respectivamente.
- Una instancia del servicio RoundRobin escuchando en el puerto p煤blico 8080.


### Implementaci贸n

La implementaci贸n se divide en dos partes, desarrollo y creaci贸n de contenedores, y despliegue de los contenedores en AWS. Haremos un breve recorrido por ambas partes.

#### Desarrollo y creaci贸n de contenedores

Dos de las tres partes que componen el proyecto fueron desarrolladas por nosotros mismos, estas son [RoundRobin](/RoundRobin) y [LogService](/LogService), su c贸digo fuente puede ser encontrado en los enlaces de cada una de ellas, y su arquitectura en el [documento de dise帽o](Lab5_AREP.pdf).

Luego de crear con 茅xito las partes y probarlas localmente en los servidores SparkWeb de cada una de ellas, procedimos a encapsularlas en contenedores Docker como se muestra continuaci贸n.

Inicialmente, construimos los contenedores de manera local con los siguientes comandos:

![Build LogService](/img/build_logservice.png)
![Build RoundRobin](/img/build_roundrobin.png)

Luego de esto, creamos dos repositorios en dockerhub, uno para cada una de nuestras im谩genes, y las subimos con los siguientes comandos:

Mapeamos los repositorios a nuestros contenedores locales

![Tag LogService](/img/tag_logservice.png)
![Tag RoundRobin](/img/tag_roundrobin.png)

Subimos los contenedores a los repositorios

![Push LogService](img/push_logservice.png)
![Push RoundRobin](img/push_roundrobin.png)

Ahora que tenemos los contenedores subidos en nuestro repositorio, vamos a crear un archivo docker compose para instalarlos de manera sencilla en otras m谩quinas, el c贸digo de este archivo luce as铆:

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

Como podemos observar tenemos una instancia de nuestro contenedor RoundRobin, el cu谩l har谩 el rol de servidor p煤blico y de balanceador de carga, tres instancias de nuestro contenedor LogService, los cuales se encargar谩n de escribir los logs en la base de datos, y una instancia de MongoDB que es nuestra base de datos no relacional.

Gracias a que **docker-compose** crea una red interna con un servicio DNS, podemos conectarnos entre contenedores por medio de sus propios nombres, un ejemplo de conexi贸n a la base de datos corriendo en el contenedor llamado *mongodb* es el siguiente:

![Conexi贸n MongoDB](img/conn_mongodb.png)

---

#### Despliegue de contenedores en AWS

Gracias a que subimos nuestro repositorio en Github, fu茅 muy sencillo clonarlo en nuestra m谩quina virtual en AWS y ejecutar el *docker-compose* para instalar todos los contenedores. Inicialmente, instalamos el *docker-compose* con los siguiente comando:

![Install docker-compose](/img/compose-inst.png)
![Execute docker-compose](img/compose-exec.png)

Luego de esto, clonamos nuestro repositorio de Github, nos cambiamos a la carpeta e instalamos nuestros contenedores por medio de *docker-compose* de la siguiente manera:

![Deploy docker-compose](img/compose-deploy.png)

Una vez los contenedores se encuentran desplegados y en ejecuci贸n, podemos observar el estado de la red interna por medio del siguiente comando, y arroj谩ndonos el siguiente resultado:

``` bash
docker network inspect <Network name>
```
![Local Network](img/network.png)

Ac谩 nos pudimos dar cuenta de que todos los contenedores se encuentran dentro de la misma red y se pueden comunicar entre ellos. Por 煤ltimo, debemos abrir el puerto p煤blico de la m谩quina virtual, en nuestro caso el 8080, para que de esta forma sea accesible por cualquier persona.

![Open Port](img/open_port.png)

#### Resultado

Vamos a observar que sucede al agregar un nuevo registro:

![Register Before Add](img/reg_before.png)
![Register After Add](img/reg_after.png)

Como pudimos observar, se adicion贸 y se retorn贸 correctamente el registro junto con los que se hab铆an registrado anteriormente.

### Descarga del proyecto

Clone el proyecto utilizando el siguiente comando:

```
git clone https://github.com/danielrincon-m/AREP_LAB5.git
```

## Correr las pruebas unitarias 馃И

### Prerrequisitos

Un IDE que soporte proyectos Java, o una instalaci贸n de Maven en su sistema, puede obtenerlo desde
la [p谩gina oficial.][mvnLink]

### Ejecuci贸n de pruebas

Las pruebas pueden ser ejecutadas desde la secci贸n de pruebas de su IDE o si tiene maven puede navegar a la carpeta
principal de cada uno de los dos proyectos internos y ejecutar el comando

```
mvn test
```

## Documentaci贸n del c贸digo fuente 馃寧

La documentaci贸n de los proyectos puede ser encontrada en las carpetas [LogService/docs](LogService/docs) y [RoundRobin/docs](RoundRobin/docs).

Tambi茅n puede ser generada con Maven, clonando el proyecto y ejecutando el siguiente comando:

```
mvn javadoc:javadoc
```

## Documento de dise帽o 馃搫

El documento de dise帽o del programa puede ser encontrado [aqu铆](Lab5_AREP.pdf).

## Herramientas utilizadas 馃洜锔?

* [Visual Studio Code](https://code.visualstudio.com/) - IDE de desarrollo
* [Maven](https://maven.apache.org/) - Manejo de Dependencias
* [JUnit](https://junit.org/junit4/) - Pruebas unitarias
* [GitHub](https://github.com/) - Repositorio de c贸digo
* [Docker](https://www.docker.com/) - Herramienta de encapsulamiento en contenedores
* [MongoDB](https://www.mongodb.com/es) - Base de datos
* [AWS](https://aws.amazon.com/es/) - Despliegue en la nube

## Autor 馃

**Daniel Felipe Rinc贸n Mu帽oz:** *Planeaci贸n y desarrollo del proyecto* -
[Perfil de GitHub](https://github.com/danielrincon-m)

## Licencia 馃殌

Este proyecto se encuentra licenciado bajo **GNU General Public License** - consulte el archivo [LICENSE.md](LICENSE.md)
para m谩s detalles.

<!-- 
## Acknowledgments 

* Hat tip to anyone whose code was used
* Inspiration
* etc
-->

[gitLink]: https://git-scm.com/downloads
[mvnLink]: https://maven.apache.org/download.cgi
