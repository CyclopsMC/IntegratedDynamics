gnome-terminal -- bash -c "
while true; do
    # Run the build command and capture output
    output=\$(./gradlew build 2>&1)
    echo \"\$output\"

    # Check if the build was successful
    if [[ \$? -eq 0 ]]; then
        ./gradlew runClient
        read -p 'Press Enter to close...'
        break
    fi

    # Check for Spotless error
    if echo \"\$output\" | grep -q 'spotlessJavaCheck FAILED'; then
        echo 'Spotless check failed. Press a to apply spotless fixes and retry, or q to quit.'
        read -n 1 -s -r key
        if [[ \"\$key\" == \"a\" ]]; then
            ./gradlew spotlessApply
            echo 'Spotless fixes applied. Retrying build...'
        elif [[ \"\$key\" == \"q\" ]]; then
            echo 'Exiting.'
            exit 1
        else
            echo 'Invalid option. Exiting.'
            exit 1
        fi
    else
        echo 'Build failed for other reasons. Press Enter to exit.'
        read -p 'Press Enter to close...'
        exit 1
    fi
done
"