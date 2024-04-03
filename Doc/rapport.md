# DMA - Labo 2

Loris Marzullo - Thibault Seem - Joel Matias

## 1.1. Questions d’approfondissement

### 1.1.1 Est-ce que toutes les balises à proximité sont présentes dans toutes les annonces de la librairie ? Que faut-il mettre en place pour permettre de « lisser »  les annonces et ne pas perdre momentanément certaines balises ?

```
Non, elles ne sont pas toujours toutes affichées. On peut présumer que la librairie garde en mémoire les signaux reçus pendant un temps T, et tout les temps T il nous donne la liste desdits signaux. Ce qui peut provoquer la disparition d'une des balises de la liste qui n'a pas envoyé de signal (ou que le signal n'a pas été reçu) durant cette période fixe.

Afin de régler ce problème, au lieu de remplacer la liste existante à chaque nouvel appel du callback, nous pouvons mettre à jour la liste avec les nouveaux signaux et mettre à jour les informations de ceux existants si ceux-ci sont différents.


```



### 1.1.2 Nous souhaitons effectuer un positionnement en arrière-plan, à quel moment faut-il démarrer et éteindre le monitoring des balises ? Sans le mettre en place, que faudrait-il faire pour pouvoir continuer le monitoring alors que l’activité n’est plus active ?

```
Lors de l'appel à la fonction OnCreate(), nous pouvons démarrer le monitoring des balises et l'éteindre uniquement lors de l'appel au OnDestroy(). Et afin de le garder en place en arrière-plan, il faut déléguer l'appel aux fonctions internes du smartphone. Cela va fonctionner de la même manière que les notifications étant lancées même lorsque l'application est éteinte.

Pour le faire fonctionner, il faut mettre en place un BroadCastReceiver pour la communication avec les fonctions internes du téléphone.
```



### 1.1.3 On souhaite trier la liste des balises détectées de la plus proche à la plus éloignée, quelles sont les valeurs présentes dans les annonces reçues qui nous permettraient de le faire ? Comment sont-elles calculées et quelle est leur fiabilité ?

```
Une des valeurs envoyées dans les annonces est la distance. La distance est une estimation basée sur la force du signal bluethoot reçue du beacon au smartphone. La fiabilité est très mauvaise. Jusqu'à deux mètres, les données sont fiables de plus ou moins un mètre tandis qu'à vingt mètres, sont fiables à plus ou moins 10 à 40 mètres.
```

### 2.1.1 Comment pouvons-nous déterminer notre position ? Est-ce uniquement basé sur notion de
proximité étudiée dans la question 1.1.3, selon vous est-ce que d’autres paramètres peuvent
être pertinents ?

```
Si on a une seule balise, nous n'avons pas vraiment d'autres choix que de soit calculer la distance avec la puissance du signal, soit utiliser la valeur de distance envoyée par la balise. 
Si nous avons 3 balises ou plus, il serait possible de faire une trilatération afin de pouvoir se repérer dans un plan. Cependant, cela ne serait pas excessivement précis, puisque chaque obstacle poserait problème en modifiant la puissance du signal.
```

### 2.1.2 Les iBeacons sont conçus pour permettre du positionnement en intérieur. D’après l’expérience que vous avez acquise sur cette technologie dans ce laboratoire, quels sont les cas d’utilisation pour lesquels les iBeacons sont pleinement adaptés (minimum deux) ? Est-ce que vous voyez des limitations qui rendraient difficile leur utilisation pour certaines applications ?

```
Nous avons pensé à deux cas d'utilisations. 

Le premier serait de permettre à une personne de s'orienter dans une manifestation (festival, salon, forum) à l'aide d'une application. Des iBeacon seraient disposés, par exemple dans le cas du salon de l'auto, à chaque stands. Ainsi, l'app pourrait calculer la position de l'utilisateur dans le salon, et lui indiquer un chemin vers un autre stand.

La seconde façon de les utiliser serait dans une exposition. Un iBeacon serait placé à chaque point d'intérêt (une ouvre d'art, une voiture, etc.). Lorsqu'une personne dotée de l'app de l'exposition s'approcherait, l'app lui afficherait des informations à propos du point d'intérêt. 

La localisation à l'aide des iBeacon possède certain désaventages. Le premier est que, puisque la distance est calculée à l'aide de la force du signal, si un obstacle se tient entre nous et la balise, la mesure de distance risque d'être totalement faussée. Par exemple, si on reprend la seconde façon de les utiliser, si les oeuvres (et les iBeacon) sont enfermés dans des boites en verre par exemple, on risque de ne pas détecter l'ibeacon comme étant proche de nous. 

Un autre problème est la précision des iBeacon. Ils ne sont en effet pas très précis et ne peuvent donc pas être utilisé pour obtenir un positionnement exact.

Enfin, il nous a semblé que les smartphone ne réagissaient pas tous de manière similaire aux balises, n'obtenant pas toujours la même distance. Cela rend compliqué l'application systématique d'une librairie.
```

