#!/usr/bin/env bash

###  ------------------------------- ###
###  Helper methods for BASH scripts ###
###  ------------------------------- ###

realpath () {
(
  TARGET_FILE="$1"

  cd "$(dirname "$TARGET_FILE")"
  TARGET_FILE=$(basename "$TARGET_FILE")

  COUNT=0
  while [ -L "$TARGET_FILE" -a $COUNT -lt 100 ]
  do
      TARGET_FILE=$(readlink "$TARGET_FILE")
      cd "$(dirname "$TARGET_FILE")"
      TARGET_FILE=$(basename "$TARGET_FILE")
      COUNT=$(($COUNT + 1))
  done

  if [ "$TARGET_FILE" == "." -o "$TARGET_FILE" == ".." ]; then
    cd "$TARGET_FILE"
    TARGET_FILEPATH=
  else
    TARGET_FILEPATH=/$TARGET_FILE
  fi

  # make sure we grab the actual windows path, instead of cygwin's path.
  if ! is_cygwin; then
    echo "$(pwd -P)/$TARGET_FILE"
  else
    echo $(cygwinpath "$(pwd -P)/$TARGET_FILE")
  fi
)
}

# TODO - Do we need to detect msys?

# Uses uname to detect if we're in the odd cygwin environment.
is_cygwin() {
  local os=$(uname -s)
  case "$os" in
    CYGWIN*) return 0 ;;
    *)  return 1 ;;
  esac
}

# This can fix cygwin style /cygdrive paths so we get the
# windows style paths.
cygwinpath() {
  local file="$1"
  if is_cygwin; then
    echo $(cygpath -w $file)
  else
    echo $file
  fi
}

# Make something URI friendly
make_url() {
  url="$1"
  local nospaces=${url// /%20}
  if is_cygwin; then
    echo "/${nospaces//\\//}"
  else
    echo "$nospaces"
  fi
}

reverse_word() {
  curl -s -XGET http://localhost:8080/reverse?q="$1"
  #--data-urlencode "q=$1"
}

ensure_file_exists_or_exit() {
    if [ -f "$1" ]
    then
      echo "File exists: $1"
    else
      echo "Required file doesn't exist or cannot be created: $1"
      exit -1
    fi
}

echo "$0"
echo "dirname: $(dirname "$0")"
echo "basename: $(basename "$0")"
echo "realpath: $(realpath "$0")"

declare -r top_word_count=5
declare -r delim=" "

declare -r real_script_path="$(realpath "$0")"
declare -r solution_home="$(realpath "$(dirname "$real_script_path")")"
echo "solution_home: $solution_home"
declare -r data_path="$solution_home/../data"
declare -r input_file_path="$data_path/in/jokes.txt"
declare -r output_file_path="$data_path/out/out.txt"
declare -r log_path="$data_path/_logs"
declare -r spray_service_log_path="$log_path/spray.log"
declare -r akka_service_log_path="$log_path/akka.log"

echo "input_file_path: $input_file_path"
echo "output_file_path: $output_file_path"

declare -r spray_service_assembly_jar="$solution_home/../spray-service/target/scala-2.11/spray-service-assembly-1.0.jar"
echo "spray_service_assembly_jar: $spray_service_assembly_jar"

declare -r akka_service_assembly_jar="$solution_home/../akka-service/target/scala-2.11/akka-service-assembly-1.0.jar"
echo "akka_service_assembly_jar: $akka_service_assembly_jar"

echo "***** Checking preconditions *****"

ensure_file_exists_or_exit "$input_file_path"

touch "$output_file_path"
ensure_file_exists_or_exit "$output_file_path"

ensure_file_exists_or_exit  "$spray_service_assembly_jar"

ensure_file_exists_or_exit  "$akka_service_assembly_jar"

touch "$spray_service_log_path"
ensure_file_exists_or_exit "$spray_service_log_path"

touch "$akka_service_log_path"
ensure_file_exists_or_exit "$akka_service_log_path"

echo "***** Preconditions check OK *****"


declare spray_service_response="$(reverse_word "OK")"
echo "spray_service_response: $spray_service_response"

if [ "$spray_service_response" != "KO" ] ; then
    echo "Starting spray service ..."
    nohup java -jar "$spray_service_assembly_jar" 2>&1 > "$spray_service_log_path" &
    sleep 5s
else
    echo "spray service seems to be already running ..."
fi

echo "Starting akka service ..."
java -jar "$akka_service_assembly_jar" "$input_file_path" "$output_file_path" 2>&1 > "$akka_service_log_path"

echo "***** Top $top_word_count words with counts *****"

cat "$output_file_path" | sort -nr -t"$delim" -k2 | head -"$top_word_count"

echo "***** Top $top_word_count reversed *****"

cat "$output_file_path" | sort -nr -t"$delim" -k2 | head -"$top_word_count" | awk -F"$delim" '{print $1 }' |
    while read WORD ; do
        echo "$WORD was reversed to: $(reverse_word "$WORD")"
    done

echo "***** Finished *****"



