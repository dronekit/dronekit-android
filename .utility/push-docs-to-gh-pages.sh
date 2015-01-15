#!/bin/bash

if [ "$TRAVIS_REPO_SLUG" == "DroidPlanner/3DRServices" ] && [ "$TRAVIS_JDK_VERSION" == "oraclejdk7" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "documentation" ]; then

  echo -e "Publishing html docs...\n"

  cp -R ClientLib/mobile/build/docs/javadoc $HOME/javadoc-latest
  cp -R doc/_build $HOME/guide-latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/DroidPlanner/3DRServices gh-pages > /dev/null

  cd gh-pages

  ## Clean and update javadoc
  git rm -rf ./javadoc
  cp -Rf $HOME/javadoc-latest ./javadoc

  ## Clean and update guide doc
  git rm -rf ./guide_doc
  cp -Rf $HOME/guide-latest ./_guide_doc

  git add -f .
  git commit -m "Lastest documentation on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null

  echo -e "Published documentation to gh-pages.\n"
  
fi
