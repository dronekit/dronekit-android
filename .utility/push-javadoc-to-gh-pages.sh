#!/bin/bash

if [ "$TRAVIS_REPO_SLUG" == "DroidPlanner/3DRServices" ] && [ "$TRAVIS_JDK_VERSION" == "oraclejdk7" ] && ["$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "version_1_1_6" ]; then

  echo -e "Publishing javadoc...\n"

  cp -R ClientLib/mobile/build/docs/javadoc $HOME/javadoc-latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/DroidPlanner/3DRServices gh-pages > /dev/null

  cd gh-pages
  git rm -rf ./javadoc
  cp -Rf $HOME/javadoc-latest ./javadoc
  git add -f .
  git commit -m "Lastest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null

  echo -e "Published Javadoc to gh-pages.\n"
  
fi
