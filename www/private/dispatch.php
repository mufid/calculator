<?
require_once("/home/preda/www/MobileUserAgent.php");
$mua = new MobileUserAgent();
$isMobile = $mua->success();
if (!$isMobile) {
    if (isset($_SERVER['HTTP_PROFILE']) || isset($_SERVER['HTTP_X_WAP_PROFILE'])) {
        $isMobile=1;
    }
}

if ($isMobile) {
header('Content-Type: application/xhtml+xml; charset=utf-8');
header('Cache-Control: max-age=3600, public');
$media = 'handheld';
} else {
header('Content-Type: text/html; charset=utf-8');
$media = 'screen';
}

readfile("/home/preda/www/header.html");
$self = "/".end(split("/", $_SERVER['PHP_SELF']));
#$self = $_SERVER['REQUEST_URI'];
$root = $_SERVER['DOCUMENT_ROOT'];
//echo "hello", getmypid();
//echo $file;
$file = $root.'/private/'.$media.$self;
readfile($file);
?>
