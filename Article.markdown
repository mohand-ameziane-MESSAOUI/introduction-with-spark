# débuter avec Apache Spark

Cet article est dédié à Apache Spark. Qu’est-ce que Spark, comment installé spark sur Windows et Linux ensuite nous verrons quelques APIs de spark et quelques exemples sur ses APIs  

## Introduction 
**Apache spark** est un Framework open source de traitement de données volumineuses dédié au Big Data qui permet aux développeurs d’effectuer un traitement de données complexe de manière distribuée (cluster computing) et qui propose une api dite « fonctionnelle » qui nous donne la possibilité de faire des processing de type maps et aggregations

## Librairies de Spark : 
Spark nous propose des différentes librairies officielles qu’on peut manipuler :
	
  1) **Spark SQL** : permet de requêter les RDDs en SQL

  2) **Spark Streaming** : permet d’écrire des applications streaming

  3) **MLlib** : reprennent des algorithmes très performent du machine learning 

  4) **GraphX** : permet de faire des opérations sur les RDDs à la manière d’un graph


## Installation de scala et spark sur Windows 

### Installation de scala : 
1)	Télécharger scala à partir du lien : https://www.scala-lang.org/download/
2)	Définir les variables d’environnement : 

  a. SCALA_HOME => C: \ Program Files (x86) \ scala
  
  b. PATH => C: \ Program Files (x86) \ scala \ bin
  


### Installation de spark :
1)	Télécharger spark à partir du lien : https://spark.apache.org/downloads.html
2)	Télécharger Windows Utilities à partir du lien : https://github.com/steveloughran/winutils/tree/master/hadoop-2.7.1
3)	Définir les variables d’environnement : 

   a. HADOOP_HOME => D:\spark-2.0.1-bin-hadoop2.7
   
   b. SPARK_HOME => D:\spark-2.0.1-bin-hadoop2.7\bin
   
   c. PATH => D:\spark\spark-2.0.1-bin-hadoop2.7\bin


## Installation de scala et spark sur linux 
### Installation de scala : 
	sudo apt-get update 
	sudo apt install scala


### Installation de spark :
1)	Télécharger spark avec les lignes de commandes suivantes : 

    wget http://d3kbcqa49mib13.cloudfront.net/spark-2.2.0-bin-hadoop2.7.tgz
    
    tar xvf spark-2.2.0-bin-hadoop2.7.tgz

2)	Définir les variables d’environnement :

     export PATH=$PATH:/usr/local/spark/bin


## SparkSession 
**SparkSession** est le point d’entrée dans L’API spark et toutes ses fonctionnalités 

**Creation de la SparkSession avec scala** : 
    
    implicit val spark: SparkSession = SparkSession
          .builder()
          .master("local[*]")
          .getOrCreate()
    

**Builder** : qui est un constructeur pour la création de la **SparkSession** 

**Master** : permet de définir l’URL principale de Spark à laquelle se connecter, dans notre exemple c’est « local » pour s'exécuter localement

**getOrCreat** : c’est pour obtenir la sparkSession si elle existe ou bien la créer si elle n’existe pas  

# Spark APIs

## Rdd	
« Resilient Distributed Dataset » c'est une structure de données résiliente et distribuée sur les différents executors d’un cluster sur lesquels on applique des transformations et des calculs en mémoire sur de grands clusters. L’élément de base d’un RDD est un record

## DataFrame
Contrairement à un RDD, les données sont organisées en colonnes nommées. On manipule des « row » donc c’est un RDD avec SCHEMA.

Voici quelques exemples de transformation et de calculs :

**Exemple 1 :** 
	On prend un fichier csv de personne on le transform en DataFrame :

     spark.read
          .format("csv")
          .option("delimiter", ";")
          .option("header", "true")
          .load(path)

**résultat :**
 
![](https://github.com/mohand-ameziane-MESSAOUI/introduction-with-spark/blob/master/images/personDF.PNG)
          
**Exemple 2 :** 
	On veut avoir que les personnes qui habitent à paris, pour cela on va filtrer notre DataFrame.

     personDF
          .filter(person=>person.getString(3) == city)
          
**résultat :**
 
![](https://github.com/mohand-ameziane-MESSAOUI/introduction-with-spark/blob/master/images/personParis.PNG)
          
**Exemple 3 :** 
	On veut avoir le nombre de personnes qui habitent dans chaque ville, pour cela on va grouper par ville « groupe By » et puis faire un « count ».

    personDF
        .groupBy("ville")
        .count()


**résultat :**
 
![](https://github.com/mohand-ameziane-MESSAOUI/introduction-with-spark/blob/master/images/countPersonCity.PNG)
 
## Dataset 

Ce qui différencier du DataFrame c’est qu’avec les Dataset on manipule plutôt des objets et aussi il est optimisé par catalyst, et pour l’utiliser il faut un encoder qui à partir des classes il importe le schéma.

**Encoder**: Est un objet scala qui nous permet de déduire le schéma à partir d’une class, il est utilisé en Spark pour passer d’un DataFrame à un Dataset. 

Toujours avec le même fichier csvvoici quelques exemples de transformation et de calculs :

**Exemple 1 :**

1.Créer une case class Person et son encoder

    case class Person (lastName: String, firstName: String, dateB: String, city: String)
    
    implicit val encdPersonne = Encoders.product[Person]
 
 
2.Passé d’une Dataframe de « row » a un Dataset de « Personne »
   
       personDF
           .map(person => Person(person.getString(0),person.getString(1),person.getString(2),person.getString(3)))
 
**résultat :**
   
![](https://github.com/mohand-ameziane-MESSAOUI/introduction-with-spark/blob/master/images/personParisDS.PNG)
 
Exemple 2 : 

Avoir que les personnes qui habitent à Paris, pour cela on va filtrer notre DataFrame mais cette fois on va manipuler des objets ce qui rend la tâche plus facile et plus lisible. 

    personDS
          .filter(person => person.city == city)
 
**résultat :**
   
![](https://github.com/mohand-ameziane-MESSAOUI/introduction-with-spark/blob/master/images/personParisDS.PNG)
        
## Spark SQL 
**Spark SQL :** est un module d’Apache Spark qui permet de travailler avec des données structurées il consiste à mélanger des programmes spark avec des requêtes SQL 
Reprennent les exemples précédents et les faire en spark SQL 

1.	Créer une « vue » depuis un Dataset ensuite l’utiliser comme une table dans Spark SQL :

    personDS
        .createOrReplaceTempView("person")
    
2.	Avoir que les personnes qui habitent à Paris :

     `` ''
    spark.sql("""select * from person where city = "paris"""".stripMargin)
     `` ''
      
**résultat :**
 
![](https://github.com/mohand-ameziane-MESSAOUI/introduction-with-spark/blob/master/images/personParisSQL.PNG)
 
3.	Avoir le nombre de personnes qui habitent dans chaque ville : 

    `` ''
     spark.sql("""select city, count(*) as count from person group by city""".stripMargin)
    `` ''    

**résultat :**
 
![](https://github.com/mohand-ameziane-MESSAOUI/introduction-with-spark/blob/master/images/countPersonCitySQL.PNG)
 
