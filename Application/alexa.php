<?php

$next = true;
$url = "http://www.alexa.com/topsites/countries/SE";
$sites_list = array();
$args = getopt("n:");
$number_of_sites = intval($args["n"]);

  if ($number_of_sites > 500 || $number_of_sites < 0) {
    $number_of_sites = 20;
  }

  while ($next) {
    $doc = new DomDocument;

    @$doc->loadHTMLFile($url);

    $data = $doc->getElementById('topsites-countries');

    $my_data = $data->getElementsByTagName('div');

    $xpath = new DOMXpath($doc);

    $get_websites = $xpath->query('//span[@class="small topsites-label"]');

    foreach ($get_websites as $sites) {
        $sites_list[] = $sites->nodeValue . "\n";
    }

    $is_next = $xpath->query('//a[@class="next"]');

    if ($is_next->item(0)) {
        $url = "http://www.alexa.com" . $is_next->item(0)->getAttribute("href");
    } else {
        $next = NULL;
    }
    
    if (count($sites_list) >= $number_of_sites) {
        break;
    }
    
  }

  for ($i = 0; $i < $number_of_sites; $i++) {
    echo $sites_list[$i];
  }

?>
